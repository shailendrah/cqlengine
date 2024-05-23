package oracle.osa.cqlx;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestCqlxReport {

	static String outputFilePath = System.getProperty("test.outputFolder");
        
        @Parameters(name = "Test{index}: {0}")
	public static Collection<Object[]> getFiles() {
		Collection<Object[]> params = new ArrayList<Object[]>();

		File dir;
		try {
			dir = new File(outputFilePath);
			File[] files = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".suc") || name.endsWith(".dif") && !name.startsWith("stat_");
				}
			});
			for (File f : files) {
				Object[] arr = new Object[] { f };
				params.add(arr);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return params;
	}

	private File file;

	public TestCqlxReport(File file) {
		this.file = file;
	}

	@Test
	public void testCqlxTestcase() throws IOException {

		String caseName = file.getName();

		if (caseName.endsWith(".dif")) {
			//print out the first difference
			String content = readFirstDiff(outputFilePath + "/" + caseName, caseName);
			System.err.println(content);
			assertTrue(caseName, false);
		}

		if (file.getName().toLowerCase().endsWith(".suc")) {
			assertTrue(file.getName().toLowerCase(), true);
		}
	}

	static String readFirstDiff(String path,String caseName) throws IOException {
		String expectString = "";
		String actualString = "";
		boolean exp = false;
		boolean act = false;

		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(new File(path));

		// now read the file line by line...
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();			
			if (line.trim().startsWith("<") && !exp) {
				exp = true;
				expectString = line.trim().substring(1);
			} else if (line.trim().startsWith(">") && !act) {
				act = true;
				actualString = line.trim().substring(1);
			}
		}
		
		StringBuffer s = new StringBuffer();
		s.append(caseName.subSequence(0, caseName.length()-4)).append(System.getProperty("line.separator"));
		s.append("Expected Result:").append(expectString);
		s.append("   ").append(System.getProperty("line.separator"));
		s.append("Actual Result:").append(actualString);

		return s.toString();
	}

}
