package demo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.RestTemplates;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class ApplicationTests {

	@Test
	public void contextLoads() {
		assertEquals("{\"message\":\"Hello World\"}",
				RestTemplates.get().getForObject("http://localhost:5050/", String.class));
	}

}
