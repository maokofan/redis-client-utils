package maoko.redis.utils.entity;

import maoko.common.model.net.CusHostAndPort;

/**
 * 特定主机内存详细信息
 *
 * @author fanpei
 * @date 2016年7月12日下午2:42:42
 */
public class HostMemInfo {

    private MemInfo mc;
    private CusHostAndPort host;

    public HostMemInfo() {

    }

    public HostMemInfo(CusHostAndPort host, MemInfo mc) {
        this.host = host;
        this.mc = mc;
    }

    public MemInfo getMc() {
        return mc;
    }

    public CusHostAndPort getHost() {
        return host;
    }

    @Override
    public String toString() {
        return mc.toString();
    }

}
