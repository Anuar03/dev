package kz.report.dev;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.arta.synergy.forms.common.object.ASFDataWrapperExt;
import kz.arta.synergy.forms.common.util.rest.operations.AsfDataApi;
import kz.report.dev.utils.httputils.HttpSynergyUtils;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;

import javax.xml.bind.DatatypeConverter;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;

@SpringBootTest
class DevApplicationTests {

//	@Test
//	void test() throws Exception {
//		HttpSynergyUtils utils = new HttpSynergyUtils("admincrm", Base64.getEncoder().encodeToString("Adm1nCRM".getBytes()));
//		URL url = new URL("http://test-lis.nce.kz/Synergy/rest/api/registry/data_ext?registryCode=reestr_registratsiya_ob_ekta");
//		String login = "admincrm";
//		String psw = "Adm1nCRM";
//		String encoded = DatatypeConverter.printBase64Binary((login + ":" + psw).getBytes());
//		InputStream stream = utils.openGetConnection(url, "Basic " + encoded).getInputStream();
//		Scanner scanner = new Scanner(stream).useDelimiter("\\A");
//
//		String s = scanner.hasNext() ? scanner.next() : "";
////				IOUtils.toString(stream, Charset.forName("windows-1251"));
//		ObjectMapper mapper = new ObjectMapper();
//		JsonNode jsonNode = mapper.readTree(s);
//		int count = 0;
//		for (Iterator<JsonNode> iterator = jsonNode.get("result").elements(); iterator.hasNext();) {
//			count++;
//			JsonNode node = iterator.next();;
//		}
//		System.out.println(count);
//	}
//
//
//
//	@Test
//	void contextLoads() throws Exception {
//		AsfDataApi asfDataApi = new AsfDataApi("http://test-lis.nce.kz/Synergy", "Basic " + encodeBase64String(("admincrm" + ":" + "Adm1nCRM").getBytes()));
//		ASFDataWrapperExt asfDataWrapperExt = asfDataApi.getAsfData("292757");
//		System.out.println(asfDataWrapperExt.getData("dynamic_table").getData());
//	}

	@Test
	void test1() {
		String date = "2020-06-05 00:00:00.";
		System.out.println(date.length());
		if (date.length() > 19) {
			System.out.println(date.substring(0, 19));
		} else {
			System.out.println("bbb");
		}
	}
}
