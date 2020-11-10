package ch.xwr.seicentobilling.business.helper;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageResizer {
	private File f2 = null;
	private final File inpFile;

	public ImageResizer(final File inpFile) {
		this.inpFile = inpFile;
	}

	private void resizeFile(final File outputFile, final int scaledWidth, final int scaledHeight) throws IOException {
		// reads input image
		final BufferedImage inputImage = ImageIO.read(this.inpFile);
		// creates output image
	    final BufferedImage outputImage = new BufferedImage(scaledWidth, scaledHeight, inputImage.getType());

	    // scales the input image to the output image
	    final Graphics2D g2d = outputImage.createGraphics();
	    g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
	    g2d.dispose();

	    // extracts extension of output file
	    final String formatName = outputFile.getName().substring(outputFile.getName().lastIndexOf(".") + 1);

	    // writes to output file
	    this.f2 = outputFile;
	    ImageIO.write(outputImage, formatName, this.f2);

	    this.inpFile.delete();
	}


//	private File getTempFile() {
//		File temp = null;
//        try {
//            temp = File.createTempFile("exp_", ".jpg");
//            System.out.println("Temp file : " + temp.getAbsolutePath());
//
//            final String absolutePath = temp.getAbsolutePath();
//            final String tempFilePath = absolutePath
//                  .substring(0, absolutePath.lastIndexOf(File.separator));
//
//        } catch (final IOException e) {
//            e.printStackTrace();
//        }
//        return temp;
//	}

	public void resize(final int maxSizeByte) {
		int ih = 800;
		int iw = 1280;

		final long isize = this.inpFile.length();

		//factor = Wurzel aus sizeR / size
		final double fact = (double) maxSizeByte / isize;
		final double sf = Math.sqrt(fact);

		BufferedImage inputImage;
		try {
			inputImage = ImageIO.read(this.inpFile);
			ih = inputImage.getHeight();
			iw = inputImage.getWidth();

			iw = (int) (iw * sf);
			ih = (int) (ih * sf);

		} catch (final IOException e) {
			e.printStackTrace();
		}

		resize(iw, ih);
	}

	public void resize(final int iW, final int iH) {
		String fnews = this.inpFile.getAbsolutePath();
		final String ext = getExtension(fnews);
		final String suffix = "" + iW + "x" + iH + ext;
		fnews = fnews.replace(ext, suffix);

		try {
			resizeFile(new File(fnews), iW, iH);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static String getExtension(final String fileName) {
		String extension = "";

		final int i = fileName.lastIndexOf('.');
		if (i >= 0) {
		    extension = fileName.substring(i);
		}
		return extension;
	}

	public File getResizedFile() {
		return this.f2;
	}
}
