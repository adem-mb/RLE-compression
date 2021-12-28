package compression.rle;

public class Pixel {


	public Pixel(int rgb, int frequency) {
		
		this.rgb = rgb;
		this.frequency = frequency;
	}
	public void Pixel(int rgb, int frequency,int color ) {
		
		int skipedBits=32-color;
		
	}

	public int getRgb() {
		return rgb;
	}

	public void setRgb(int rgb) {
		this.rgb = rgb;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	private int rgb;
	private int frequency;
	

}
