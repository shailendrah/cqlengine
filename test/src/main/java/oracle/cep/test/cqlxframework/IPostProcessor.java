package oracle.cep.test.cqlxframework;

import java.util.List;

public interface IPostProcessor {
	List<String> postProcess(List<String> input);
}
