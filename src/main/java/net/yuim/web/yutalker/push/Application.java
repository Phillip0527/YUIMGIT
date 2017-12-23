package net.yuim.web.yutalker.push;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import net.yuim.web.yutalker.push.service.AccountService;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.logging.Logger;

/**
 * Created by Phillip on 2017/12/23.
 */
public class Application extends ResourceConfig {
    public Application() {
        //注册逻辑处理的包名
//        packages("net.yuim.web.yutalker.push.service");
        packages(AccountService.class.getPackage().getName());
        //注册JSON解析器
        register(JacksonJsonProvider.class);
        //注册日志打印输出
        register(Logger.class);
    }
}
