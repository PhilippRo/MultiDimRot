package malte0811.multiDim.tickHandlers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

import malte0811.multiDim.addons.DimRegistry;
import malte0811.multiDim.addons.TickHandler;
import malte0811.multiDim.solids.Solid;

import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.codecs.h264.H264Utils;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.Brand;
import org.jcodec.containers.mp4.MP4Packet;
import org.jcodec.containers.mp4.TrackType;
import org.jcodec.containers.mp4.muxer.FramesMP4MuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.jcodec.scale.AWTUtil;
import org.jcodec.scale.RgbToYuv420;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class MediaHandler extends TickHandler {
	boolean screenShot = false;
	boolean rec = false;
	int framesLeft = 0;
	int maxFrames = 0;
	File videoOut = null;
	public static MediaHandler instance;

	private SeekableByteChannel ch;
	private Picture toEncode;
	private RgbToYuv420 transform;
	private H264Encoder encoder;
	private ArrayList<ByteBuffer> spsList;
	private ArrayList<ByteBuffer> ppsList;
	private FramesMP4MuxerTrack outTrack;
	private ByteBuffer _out;
	private int frameNo;
	private MP4Muxer muxer;

	public MediaHandler() {
		instance = this;
	}

	@Override
	public void handleTick(Solid d, double[] renderoptions) {
		if (screenShot) {
			String sep = DimRegistry.getFileSeperator();
			String file = DimRegistry.getUserDir() + sep + "screenshots" + sep
					+ Long.toString(System.currentTimeMillis()) + ".jpeg";
			handleScreenShot(new File(file));
			screenShot = false;
		}
		if (rec) {
			String sep = DimRegistry.getFileSeperator();
			if (framesLeft == 0) {
				String file = DimRegistry.getUserDir() + sep + "tmp" + sep
						+ "screen";
				for (int i = 0; i < maxFrames; i++) {
					try {
						BufferedImage tmp = ImageIO.read(new File(file
								+ Integer.toString(i) + ".jpeg"));
						encodeImage(tmp);
					} catch (IOException e) {
						System.out
								.println("An error occured while encoding image "
										+ i);
						DimRegistry.getLogger().logException(e);
					}
				}
				try {
					finish();
				} catch (IOException e1) {
					System.out
							.println("An error occured while creating the video");
					DimRegistry.getLogger().logException(e1);
				}
				// delete files
				for (int i = 0; i < maxFrames; i++) {
					try {
						Files.delete(Paths.get(file + Integer.toString(i)
								+ ".jpeg"));
					} catch (IOException e) {
						DimRegistry.getLogger().logException(e);
					}
				}

				rec = false;
			} else {
				String file = DimRegistry.getUserDir() + sep + "tmp" + sep
						+ "screen" + (maxFrames - framesLeft) + ".jpeg";
				handleScreenShot(new File(file));
				framesLeft--;
			}
		}
	}

	public void screenShot() {
		screenShot = true;
	}

	private void handleScreenShot(File save) {
		GL11.glReadBuffer(GL11.GL_FRONT);
		int width = Display.getDisplayMode().getWidth();
		int height = Display.getDisplayMode().getHeight();
		int bpp = 4;
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
		GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA,
				GL11.GL_UNSIGNED_BYTE, buffer);
		String format = "JPG";
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int i = (x + (width * y)) * bpp;
				int r = buffer.get(i) & 0xFF;
				int g = buffer.get(i + 1) & 0xFF;
				int b = buffer.get(i + 2) & 0xFF;
				image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16)
						| (g << 8) | b);
			}
		}

		try {
			ImageIO.write(image, format, save);
		} catch (IOException e) {
			System.out
					.println("An error occured while saving a screenshot/frame");
			DimRegistry.getLogger().logException(e);
		}
	}

	public void record(int ticks, File file) throws IOException {
		videoOut = file;
		framesLeft = ticks;
		maxFrames = ticks;
		rec = true;

		this.ch = NIOUtils.writableFileChannel(file);

		transform = new RgbToYuv420(0, 0);

		muxer = new MP4Muxer(ch, Brand.MP4);

		outTrack = muxer.addTrackForCompressed(TrackType.VIDEO, 10);

		_out = ByteBuffer
				.allocate(Display.getHeight() * Display.getWidth() * 6);

		encoder = new H264Encoder();

		spsList = new ArrayList<ByteBuffer>();
		ppsList = new ArrayList<ByteBuffer>();
	}

	public void encodeImage(BufferedImage bi) throws IOException {
		if (toEncode == null) {
			toEncode = Picture.create(bi.getWidth(), bi.getHeight(),
					ColorSpace.YUV420);
		}

		for (int i = 0; i < 3; i++)
			Arrays.fill(toEncode.getData()[i], 0);
		transform.transform(AWTUtil.fromBufferedImage(bi), toEncode);

		_out.clear();
		ByteBuffer result = encoder.encodeFrame(_out, toEncode);

		spsList.clear();
		ppsList.clear();
		H264Utils.encodeMOVPacket(result, spsList, ppsList);

		outTrack.addFrame(new MP4Packet(result, frameNo, 10, 1, frameNo, true,
				null, frameNo, 0));

		frameNo++;
	}

	public void finish() throws IOException {
		outTrack.addSampleEntry(H264Utils
				.createMOVSampleEntry(spsList, ppsList));

		muxer.writeHeader();
		NIOUtils.closeQuietly(ch);
	}

}