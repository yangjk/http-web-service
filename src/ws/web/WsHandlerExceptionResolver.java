package ws.web;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: jkyang
 */
public class WsHandlerExceptionResolver implements HandlerExceptionResolver {


    private JsonEncoding encoding = JsonEncoding.UTF8;

    private ObjectMapper objectMapper = new ObjectMapper();


    public ModelAndView resolveException(HttpServletRequest httpServletRequest,
                                         HttpServletResponse httpServletResponse, Object o, Exception e) {
        try {
            ErrorInfo info = new ErrorInfo();
            info.setMessage(e.getMessage());
            info.setException(e.getClass().getName());
            StringWriter sw = new StringWriter();
            PrintWriter psw = new PrintWriter(sw);
            e.printStackTrace(psw);
            info.setStackTrace(sw.toString());
            httpServletResponse.setContentType("application/json");
            httpServletResponse.setCharacterEncoding(encoding.getJavaName());
            JsonGenerator generator = objectMapper.getJsonFactory().
                    createJsonGenerator(httpServletResponse.getOutputStream(), encoding);
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("result",info);
            objectMapper.writeValue(generator, map);
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
