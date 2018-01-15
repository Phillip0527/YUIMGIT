package net.yuim.web.yutalker.push;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import net.yuim.web.yutalker.push.provider.AuthRequestFilter;
import net.yuim.web.yutalker.push.provider.GsonProvider;
import net.yuim.web.yutalker.push.service.AccountService;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.logging.Logger;

/**
 * Created by Phillip on 2017/12/23.
 */
public class Application extends ResourceConfig {
    public Application() {
        // 注册逻辑处理的包名
//        packages("net.yuim.web.yutalker.push.service");
        packages(AccountService.class.getPackage().getName());
        // 注册我们的全局请求拦截器
        register(AuthRequestFilter.class);
        // 注册JSON解析器
//        register(JacksonJsonProvider.class);
        // 替换解析器为GsonProvider
        register(GsonProvider.class);
        // 注册日志打印输出
        register(Logger.class);
    }
}
