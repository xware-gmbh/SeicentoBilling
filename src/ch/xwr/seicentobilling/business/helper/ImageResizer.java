package ch.xwr.seicentobilling.business.helper;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

public class ImageResizer {
	private File f2 = null;

	public void resizeFile(final File inputFile, final File outputFile, final int scaledWidth, final int scaledHeight) throws IOException {
		// reads input image
		final BufferedImage inputImage = ImageIO.read(inputFile);
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
	}


	private File getTempFile() {
		File temp = null;
        try {
            temp = File.createTempFile("exp_", ".jpg");
            System.out.println("Temp file : " + temp.getAbsolutePath());

            final String absolutePath = temp.getAbsolutePath();
            final String tempFilePath = absolutePath
                  .substring(0, absolutePath.lastIndexOf(File.separator));

        } catch (final IOException e) {
            e.printStackTrace();
        }
        return temp;
	}

	public void resize(final ByteArrayOutputStream baos, final int iW, final int iH) {
		final File f1 = getTempFile();
		final File fnew = getTempFile();

		try (OutputStream outputStream = new FileOutputStream(f1)) {
			baos.writeTo(outputStream);

			outputStream.close();


		} catch (final IOException e) {
			e.printStackTrace();
		}

		try {
			resizeFile(f1, fnew, iW, iH);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public File getResizedFile() {
		return this.f2;
	}
}
