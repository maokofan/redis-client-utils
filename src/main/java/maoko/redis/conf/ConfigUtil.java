package maoko.redis.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import maoko.common.XmlParseUtil;
import maoko.common.exception.DataIsNullException;
import maoko.common.file.PathUtil;
import maoko.common.model.net.CusHostAndPort;
import maoko.common.system.AppRunPathUitl;
import maoko.redis.utils.core.RedisPoolConfig;
import maoko.redis.utils.except.CusException;
import maoko.redis.utils.except.ErrorCode;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.management.modelmbean.XMLParseException;


/**
 * 配置文件助手
 *
 * @author fanpei
 * @version 创建时间：2016年9月21日 下午4:14:07
 */
public class ConfigUtil {
    // public static String rootPath = ".";
    private final static String confPath = "config/redis.xml";
    private final static Object syncLock = new Object();// 同步锁
    private static ConfigUtil instance;
    private RedisPoolConfig redisconf;

    public RedisPoolConfig getRedisconf() {
        return redisconf;
    }

    public static ConfigUtil GetInstance() {

        synchronized (syncLock) {
            if (instance == null) {
                /*
                 * try { rootPath = URLDecoder.decode(ConfigUtil.class.getClassLoader
                 * ().getResource("").getPath(), "utf-8").replace("\\",
                 * SysDefine.FILESEPARATOR); } catch (UnsupportedEncodingException e) { }
                 */
                instance = new ConfigUtil();
            }

            return instance;
        }
    }

    /**
     * 读取配置文件
     *
     * @return
     * @throws CusException
     */
    @SuppressWarnings("rawtypes")
    public void ReadConfig() throws CusException {
        String runPath = AppRunPathUitl.getAppRunPathNew();
        String filepath = PathUtil.combinePath(runPath, confPath);
        InputStream inputStream = null;
        try {
            if (!new File(filepath).exists())
                throw new FileNotFoundException(filepath + " is not exist");

            // InputStream in =
            // ConfigUtil.class.getClassLoader().getResourceAsStream("Config.xml");
            // FileCopy.copy(in, filepath, false);

            SAXReader saxReadr = new SAXReader();
            Document document = saxReadr.read(inputStream = new FileInputStream(new File(filepath)));
            Element Redis = document.getRootElement();// 得到Redis根节点

            // 1.CloudPanSys
            Element Nodes = XmlParseUtil.getElemnt(Redis, "Nodes");
            redisconf = new RedisPoolConfig();
            {
                redisconf.setSentinel("true".equals(XmlParseUtil.getAttributeValue(Nodes, "sentinel", true)));
                List<CusHostAndPort> hosts = new ArrayList<CusHostAndPort>();
                List elehosts = Nodes.elements();
                for (Iterator it = elehosts.iterator(); it.hasNext(); ) {
                    Element HostAndPort = (Element) it.next();
                    if ("HostAndPort".equals(HostAndPort.getName())) {
                        CusHostAndPort host = new CusHostAndPort();
                        host.setIP(XmlParseUtil.getAttributeValue(HostAndPort, "IP", false));
                        host.setPort(
                                Integer.parseInt(XmlParseUtil.getAttributeValue(HostAndPort, "Port", false)));
                        hosts.add(host);
                    } else {
                        throw new CusException(ErrorCode.XmlFormatterError, "Nodes节点下存在非HostAndPort子节点");
                    }
                }
                redisconf.setHosts(hosts);
            }

        } catch (Exception e) {
            throw new CusException("读取配置文件失败", e);
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
            }
        }
    }
}
