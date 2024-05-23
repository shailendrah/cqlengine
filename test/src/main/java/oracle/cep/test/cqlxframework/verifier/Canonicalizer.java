package oracle.cep.test.cqlxframework.verifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import oracle.cep.test.cqlxframework.IPostProcessor;

public class Canonicalizer implements IPostProcessor {
	private static String TIME_SPLITTER = ":";
	private static String PLUS = "+";
	private static String MINUS = "-";
	private static String UPDATE = "U";

	HashMap<String, MapValue> map;
	List<String> output;

	private static class MapValue {
		String line;
		int count;

		MapValue(String line, int count) {
			this.line = line;
			this.count = count;
		}
	}

	@Override
	public List<String> postProcess(List<String> tuplestrs) {
		if (tuplestrs.size() == 0)
			return tuplestrs;
		output = new LinkedList<String>();
		int index = 0;
		String prevTime = new String("-1");
		String inputLine = tuplestrs.get(index);
		index++;

		String time;
		String sign;
		String rest;
		int timeLength;

		map = new HashMap<String, MapValue>();

		while (true) {
			time = (inputLine.split(TIME_SPLITTER))[0];
			timeLength = time.length();

			if (timeLength > 0) {
				sign = inputLine.substring(timeLength + 1, timeLength + 2);
				rest = inputLine.substring(timeLength + 2);

				/*
				 * System.out.println("time= " + time + " ; sign= " + sign +
				 * " ; rest= " + rest);
				 */

				if (!(prevTime.equals(time))) {
					prevTime = new String(time);
					flushMap();
				}
				try {
					updateMap(sign, rest, inputLine);
				} catch (Exception e) {
					System.out.println(e.toString());
				}

			}
			if (index >= tuplestrs.size())
				break;
			inputLine = tuplestrs.get(index++);
		}
		flushMap();
		return output;
	}

	private void flushMap() {
		// Get the values in the map
		Collection<MapValue> values = map.values();
		MapValue[] valArray = values.toArray(new MapValue[0]);
		String[] lines = new String[valArray.length];

		for (int i = 0; i < valArray.length; i++)
			lines[i] = valArray[i].line;

		Arrays.sort(lines);
		for (int i = 0; i < lines.length; i++) {
			// System.out.println(lines[i]);
			output.add(lines[i]);
		}

		map.clear();
	}

	private void updateMap(String sign, String rest, String inputLine)
			throws Exception {
		int count = 0;

		if (sign.equals(PLUS))
			count = 1;
		else if (sign.equals(MINUS))
			count = -1;
		else if (sign.equals(UPDATE))
			count = 0;
		else
			throw new Exception("Invalid sign : " + inputLine);

		MapValue v = map.get(rest);
		if (v == null) {
			v = new MapValue(inputLine, count);
			map.put(rest, v);
		} else {
			v.count = v.count + count;
			if (v.count == 0)
				map.remove(rest);
		}
	}

}
