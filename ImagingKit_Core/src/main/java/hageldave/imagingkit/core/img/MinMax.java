package hageldave.imagingkit.core.img;

import hageldave.imagingkit.core.pixel.PixelBase;

public interface MinMax<P extends PixelBase<P>> extends ImgBase<P> {

	/**
	 * Returns the index of the maximum value of the specified channel.
	 * @param channel one of {@link #channel_r},{@link #channel_g},{@link #channel_b},{@link #channel_a} (0,1,2,3)
	 * @return index of maximum value of specified channel
	 * @throws ArrayIndexOutOfBoundsException if the specified channel is not in [0,3] 
	 * or is 3 but the image has no alpha (check using {@link #hasAlpha()}).
	 * @see #getIndexOfMaxValue(int)
	 * @see #getIndexOfMinValue(int)
	 * @see #getMaxValue(int)
	 * @see #getMinValue(int)
	 */
	public default int getIndexOfMaxValue(int channel){
		int index = 0;
		P px = getPixel();
		double val = px.getValue(channel);
		for(int i = 1; i < numValues(); i++){
			if(px.setIndex(i).getValue(channel) > val){
				index = i;
				val = px.getValue(channel);
			}
		}
		return index;
	}
	
	public default int getIndexOfMinValue(int channel){
		int index = 0;
		P px = getPixel();
		double val = px.getValue(channel);
		for(int i = 1; i < numValues(); i++){
			if(px.setIndex(i).getValue(channel) < val){
				index = i;
				val = px.getValue(channel);
			}
		}
		return index;
	}
	
	public default double getMaxValue(int channel){
		int idx = getIndexOfMaxValue(channel);
		return getValueAt(channel, idx%getWidth(), idx/getWidth());
	}
	
	public default double getMinValue(int channel){
		int idx = getIndexOfMinValue(channel);
		return getValueAt(channel, idx%getWidth(), idx/getWidth());
	}
	
}
