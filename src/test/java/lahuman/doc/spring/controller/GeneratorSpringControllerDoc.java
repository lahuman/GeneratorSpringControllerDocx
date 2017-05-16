package lahuman.doc.spring.controller;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.io3.Save;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Description;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author daniel
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
@Slf4j
public class GeneratorSpringControllerDoc {
  @Autowired
  RequestMappingHandlerMapping handlerMapping;

  @Autowired
  ApplicationContext context;

  // Input docx has variables in it: ${colour}, ${icecream}
  final String inputfilepath = "/home/daniel/data/DocTemplate.docx";

  final String outputfilepath = "/home/daniel/data/resultData.docx";

  
  @Test
  public void genDoc() throws Docx4JException, JAXBException, FileNotFoundException {
    ArrayList<HashMap<String, String>> docs = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> content = null;

    Map<RequestMappingInfo, HandlerMethod> handlers = handlerMapping.getHandlerMethods();
    for (RequestMappingInfo requestMappingInfo : handlers.keySet()) {

      HandlerMethod method = handlers.get(requestMappingInfo);
      String httpMethod = requestMappingInfo.getMethodsCondition() + "";
      String requestParams = "";
      String uri = "";


      requestParams = Arrays.stream(method.getMethodParameters()).filter(
          m -> (!"javax.servlet.http.HttpServletRequest".equals(m.getParameterType().getName())))
          .filter(m -> (!"javax.servlet.http.HttpServletResponse"
              .equals(m.getParameterType().getName())))
          .filter(m -> (!"org.springframework.validation.BindingResult"
              .equals(m.getParameterType().getName())))
          .map(m -> {
            String fieldName = Arrays.stream(m.getParameterAnnotations()).filter(
                a -> (!"@org.springframework.validation.annotation.Validated".equals(a.toString())))
                .map(fm -> {
                  String name = "";
                  String s = fm.toString();
                  String[] sa = s.split("name=");
                  if (sa.length > 1) {
                    name = sa[1].substring(0, sa[1].indexOf(","));
                  }
                  return name;
                }).collect(Collectors.joining(","));
            return m.getParameterType().getName() + " " + fieldName;
          }).collect(Collectors.joining(","));

      Description annotationDescription = method.getMethodAnnotation(Description.class);
      String strDesc = "";
      if (annotationDescription != null) {
        strDesc = annotationDescription.value() + " / " + annotationDescription.toString();
      }
      // logging
      log.info("Class: {}", method.getBeanType().getName());
      log.info("Function: {}", method.getMethod().getName() + strDesc);
      log.info("URI: http://SERVER:PORT/CONTEXT{}", uri);
      log.info("METHOD: {}", httpMethod.replaceAll("\\[", "").replaceAll("\\]", ""));
      log.info("Consumer Content Type: application/json");
      log.info("Consumer Payload: {}", requestParams);
      log.info("Provider Content Type: application/json");
      log.info("Provider Payload: {}", method.getReturnType().getParameterType().getName());
      log.info("description: {}", strDesc);
      log.info("\n\n");

      content = new HashMap<String, String>();
      content.put("class_name", method.getBeanType().getName());
      content.put("function", method.getMethod().getName());
      content.put("path", uri);
      content.put("method", httpMethod.replaceAll("\\[", "").replaceAll("\\]", ""));
      content.put("c_contentType", "application/json");
      content.put("c_payload", requestParams);
      content.put("p_contentType", "application/json");
      content.put("p_payload", method.getReturnType().getParameterType().getName());
      docs.add(content);
    }

    WordprocessingMLPackage mainWordMLPackage =
        WordprocessingMLPackage.load(new java.io.File(inputfilepath));

    MainDocumentPart mainDocumentPart = mainWordMLPackage.getMainDocumentPart();
    long start = System.currentTimeMillis();
    
    for (HashMap<String, String> h : docs) {
      WordprocessingMLPackage wordMLPackage =
          WordprocessingMLPackage.load(new java.io.File(inputfilepath));
      MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

      documentPart.variableReplace(h);
      List<Object> objects = documentPart.getContent();
      for (Object o : objects) {
        mainDocumentPart.getContent().add(o);
      }
    }
    
    long end = System.currentTimeMillis();
    log.info("Time: {}" , end - start);

    // Save it
    Save save = new Save(mainWordMLPackage);
    save.save(new FileOutputStream(outputfilepath));
  }
}
