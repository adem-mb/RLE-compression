package compression.rle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.BitSet;

public class RLE {

    private static final int IMAGE_SIZE_LENGTH = 13;

    private static final int COLOR_LENGTH = 5;

    private static final int FREQUENCY_LENGTH = 8;

    public static String DecimaltoNsystem(int value, int system, int length) {
        int i = 0;
        int accumilator = 0;
        int result = value, rest;
        StringBuilder sb = new StringBuilder();
        do {
            rest = result % system;
            sb.append(rest);
            result = result / system;
            i += 1;
        } while (result != 0);
        while (sb.length() < length) {
            sb.append("0");
        }
        return sb.reverse().toString();
    }

    /**
     *
     * Unlike Integer.getBinaryFormat() function this function returns the number in binary representation and preserve the
     * number in the desired length by padding with 0
     * @param preserveBits the number of bits to preserve
     * @param number the number to convert
     * @return String representation of the desired number with the length of the preserved bits
     *
     */
    private static String getBinaryFormat(int preserveBits, int number) {

        int skipedBits = 32 - preserveBits;
        int shifter = -1 << preserveBits;
        number = number | shifter; // this is step is required to preserve the zeros
        String stringRgb = Integer.toBinaryString(number);
        StringBuilder sb = new StringBuilder();
        for (int i = skipedBits; i < 32; i++) {
            sb.append(stringRgb.charAt(i));
        }

        return sb.toString();

    }
    private static int getPixelsFrequency(ArrayList<Pixel> pixels,BufferedImage bufferedImage){
        int frequency = 0;
        int lastOne = 0;
        Pixel lastPixel = null;
        int compressedPixelsNumber=0;
        for (int i = 0; i < bufferedImage.getHeight(); i++) {
            for (int j = 0; j < bufferedImage.getWidth(); j++) {
                int rgb = bufferedImage.getRGB(j, i);
                if (rgb != lastOne || frequency >= 256) {
                    lastOne = rgb;
                    if(lastPixel !=null && lastPixel.getFrequency()==0)
                        compressedPixelsNumber++;
                    lastPixel = new Pixel(rgb, 0);
                    pixels.add(lastPixel);
                    frequency = 0;
                    frequency++;
                } else {

                    lastPixel.setFrequency(frequency);
                    frequency++;


                }
            }
        }
        return compressedPixelsNumber;
    }
    public static byte[] compress(BufferedImage bi) throws Exception {

        int width = bi.getWidth();

        int height = bi.getHeight();

        int pixelSize = bi.getColorModel().getPixelSize();

        if(pixelSize == 32){
            throw new Exception("32 bit color images are not supported");
        }

        StringBuilder sb = new StringBuilder();

        sb.append(getBinaryFormat(IMAGE_SIZE_LENGTH, width));
        sb.append(getBinaryFormat(IMAGE_SIZE_LENGTH, height));
        sb.append(getBinaryFormat(COLOR_LENGTH,pixelSize-1));

        String headerBits=sb.toString();
        ArrayList<Pixel> pixels = new ArrayList<>(width * height);

        int compressedPixelsNumber=getPixelsFrequency(pixels,bi);


        BitSet io=new BitSet(IMAGE_SIZE_LENGTH*2 + COLOR_LENGTH +(COLOR_LENGTH+FREQUENCY_LENGTH)*compressedPixelsNumber);
        for(int i=0;i<31;i++) {
            io.set(i, headerBits.charAt(i) == '1');
        }

        int realLength=31;
        int i=31;
        String temp;

        for (Pixel pixel : pixels) {
            if (pixel.getFrequency() == 0) {
                io.set(i,false);
                i++;
                temp=getBinaryFormat(pixelSize, pixel.getRgb());
                for(int j=0;j<pixelSize;j++) {
                    io.set(i, temp.charAt(j) == '1');
                    i++;
                }
           } else {
                io.set(i,true);
                i++;
                temp=DecimaltoNsystem(pixel.getFrequency(), 2, 8);
                for(int j=0;j<8;j++) {

                    io.set(i, temp.charAt(j) == '1');
                    i++;

                }
                temp=getBinaryFormat(pixelSize, pixel.getRgb());
                for(int j=0;j<pixelSize;j++) {
                    io.set(i, temp.charAt(j) == '1');
                    i++;
                }

            }

        }

        return io.toByteArray();


    }
    private static BufferedImage prepareBufferedImage(BitSet bits,int headerLength){
        BufferedImage bufferedImage = null;

        BitSet height = new BitSet(IMAGE_SIZE_LENGTH);
        BitSet width = new BitSet(IMAGE_SIZE_LENGTH);
        BitSet colorBits = new BitSet(COLOR_LENGTH);

        int hi = 0, wi = 0, color = 0;
        for (int i = 0; i <= headerLength; i++) {
            if (i < IMAGE_SIZE_LENGTH) {
                width.set(i, bits.get(i));

            } else if (i >= IMAGE_SIZE_LENGTH && i < IMAGE_SIZE_LENGTH * 2) {
                if (i == IMAGE_SIZE_LENGTH) {
                    wi = bitSetToInt(width, IMAGE_SIZE_LENGTH);
                }
                height.set(i - IMAGE_SIZE_LENGTH, bits.get(i));
                if (i == IMAGE_SIZE_LENGTH * 2 - 1)
                    hi = bitSetToInt(height, IMAGE_SIZE_LENGTH);
            } else if (i >= IMAGE_SIZE_LENGTH * 2) {
                colorBits.set(i - IMAGE_SIZE_LENGTH * 2, bits.get(i));
                if (i == headerLength - 1) {
                    color = bitSetToInt(colorBits, COLOR_LENGTH)+1;


                    if (color == 24)
                        bufferedImage = new BufferedImage(wi, hi, BufferedImage.TYPE_INT_RGB);
                    /*else if (color == 32)
                        bf = new BufferedImage(wi, hi, BufferedImage.TYPE_INT_ARGB);*/
                    else if (color == 8)
                        bufferedImage = new BufferedImage(wi, hi, BufferedImage.TYPE_BYTE_GRAY);
                    else
                        bufferedImage = new BufferedImage(wi, hi, BufferedImage.TYPE_BYTE_BINARY);


                }
            }
        }
        return bufferedImage;
    }
    public static BufferedImage decompress(byte[] bytes) {
        int headerLength = IMAGE_SIZE_LENGTH*2 + COLOR_LENGTH;
        BitSet bits = BitSet.valueOf(bytes);
        BufferedImage bf = prepareBufferedImage(bits,headerLength);

        int w = 0, h = 0;

        int hi = bf.getHeight();
        int wi = bf.getWidth();
        int color = bf.getColorModel().getPixelSize();

        int i = headerLength;
        while (hi != h + 1) {

            int frequency = 1;
            if (bits.get(i)) {
                i++;
                frequency = 0;
                int k = 0;
                for (int j = i; j < 8 + i; j++) {

                    if (bits.get(j))
                        frequency |= (1 << (8 - 1) - k);
                    k++;
                }
                i = i + 8 - 1;
                frequency++;
            }
            i++;
            int end = color + i;
            int start = i;
            int rgb = 0;
            int k = 0;
            for (int j = start; j < end; j++) {
                if (bits.get(j))
                    rgb |= (1 << (color - 1) - k);
                k++;
            }
            if (color == 8) {
                int temp = rgb;
                rgb |= temp << 16;
                rgb |= temp << 8;
            } else if (color == 1 && rgb == 1) {
                rgb |= 0xFFFFFFFF;
            }
            if (color < 32)
                rgb |= 0xFF000000;

            i = i + color;
            for (int j = 0; j < frequency; j++) {

                bf.setRGB(w, h, rgb);

                if (w + 1 == wi) {

                    w = 0;
                    h += 1;
                } else
                    w += 1;
            }

        }
        return bf;
    }

    public static int bitSetToInt(BitSet bitSet, int length) {
        int bitInteger = 0;
        int len = bitSet.length() + 1;
        for (int i = 0; i < len; i++)
            if (bitSet.get(i))
                bitInteger |= (1 << length - 1 - i);
        return bitInteger;
    }
    public static void fileCompress(File file,File destinationFile) throws Exception {
        try {
            byte[] bytes = compress(ImageIO.read(file));
            System.out.println(bytes[0]);
            destinationFile.createNewFile();
            FileOutputStream fileOutputStream  = new FileOutputStream(destinationFile);
            fileOutputStream.write(bytes);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void fileDecompression(File file,File destinationFile){
        try {
            BufferedImage bufferedImage = decompress(Files.readAllBytes(file.toPath()));
            ImageIO.write(bufferedImage, "bmp", destinationFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
