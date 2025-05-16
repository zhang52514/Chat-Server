package cn.anoxia.chat.core;

import cn.anoxia.chat.core.server.WebNettyMain;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class nettyMainRun {
    private static final Logger logger = LoggerFactory.getLogger(nettyMainRun.class);
    @Autowired
    private ConfigurableApplicationContext applicationContext;
    @Autowired
    private WebNettyMain nettyStater;

    @PreDestroy
    public void destroys() throws InterruptedException {
        if (nettyStater != null) {
            nettyStater.WorkerGroup.shutdownGracefully().sync();
            nettyStater.BossGroup.shutdownGracefully().sync();
        }

        logger.info("NettySocket->关闭成功  时间：{}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }
}
