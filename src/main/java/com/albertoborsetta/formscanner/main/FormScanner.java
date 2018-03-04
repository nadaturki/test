package com.albertoborsetta.formscanner.main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import com.albertoborsetta.formscanner.api.FormTemplate;
import com.albertoborsetta.formscanner.api.exceptions.FormScannerException;
import com.albertoborsetta.formscanner.commons.FormFileUtils;
import com.albertoborsetta.formscanner.commons.FormScannerConstants;

public class FormScanner {

	private static Logger logger;

	/**
	 * Launch the application.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		{
			Locale locale = Locale.getDefault();
			FormFileUtils fileUtils = FormFileUtils.getInstance(locale);

			File templateFile = new File("D:\\pfe\\eclipse\\img\\14.xtmpl");
			FormTemplate template = null;
			try {
				template = new FormTemplate(templateFile);
				if (!FormScannerConstants.CURRENT_TEMPLATE_VERSION.equals(template.getVersion())) {
					fileUtils.saveToFile(FilenameUtils.getFullPath("D:\\pfe\\eclipse\\img\\14.xtmpl"), template, false);
				}
			} catch (ParserConfigurationException | SAXException | IOException e) {
				logger.debug("Error", e);
				System.exit(-1);
			}
			String[] extensions = ImageIO.getReaderFileSuffixes();
			Iterator<?> fileIterator = FileUtils.iterateFiles(new File("D:\\pfe\\eclipse\\img"), extensions, false);
			HashMap<String, FormTemplate> filledForms = new HashMap<>();
			while (fileIterator.hasNext()) {
				File imageFile = (File) fileIterator.next();
				BufferedImage image = null;
				try {
					image = ImageIO.read(imageFile);
				} catch (IOException e) {
					logger.debug("Error", e);
					System.exit(-1);
				}
				FormTemplate filledForm = new FormTemplate(imageFile.getName(), template);
				try {
					filledForm.findCorners(image, template.getThreshold(), template.getDensity(),
							template.getCornerType(), template.getCrop());
					filledForm.findPoints(image, template.getThreshold(), template.getDensity(), template.getSize());
					filledForm.findAreas(image);
				} catch (FormScannerException e) {
					logger.debug("Error", e);
					System.exit(-1);
				}
				filledForms.put(FilenameUtils.getName(imageFile.toString()), filledForm);
			}

			Date today = Calendar.getInstance().getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			File outputFile = new File("D:\\pfe\\eclipse\\img" + System.getProperty("file.separator") + "results_"
					+ sdf.format(today) + ".csv");
			fileUtils.saveCsvAs(outputFile, filledForms, false);
			System.exit(0);
		}
	}
}
