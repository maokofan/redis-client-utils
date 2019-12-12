package maoko.redis.utils.entity.cluster;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务节点状态详情
 *
 * @author fanpei
 */
public class SvrNodeState {

    private StateType state;// 服务状态
    private List<SvrNodeInfo> nodes;

    public StateType getState() {
        return state;
    }

    public void setState(StateType state) {
        this.state = state;
    }

    public SvrNodeState() {
        state = StateType.OFF;
        nodes = new ArrayList<SvrNodeInfo>();
    }

    public List<SvrNodeInfo> getNodes() {
        if (nodes == null || nodes.isEmpty())
            return null;
        else
            return new ArrayList<SvrNodeInfo>(nodes);
    }

    public void addNode(SvrNodeInfo node) {
        nodes.add(node);
    }

    public void setNodes(List<SvrNodeInfo> nodes) {
        this.nodes = nodes;
    }

    @Deprecated
    public void clear() {
        if (nodes != null)
            nodes.clear();
    }

    /**
     * 是否异常
     *
     * @return
     */
    public boolean isTrouble() {
        if (StateType.Normal != state)
            return true;

        /*
         * if (nodes != null) { for (SvrNodeInfo nodeInfo : nodes) { if (StateType.Normal
         * != nodeInfo.getState() && nodeInfo.isMaster()) return true; } }
         */

        return false;
    }

    @Override
    public boolean equals(Object arg0) {
        if (arg0 == null)
            return false;
        if (!arg0.getClass().equals(SvrNodeState.class))
            return false;
        SvrNodeState other = (SvrNodeState) arg0;
        if (this.state != other.state)
            return false;
        if (this.nodes != null && other.nodes != null) {
            int size = this.nodes.size();
            if (size != other.nodes.size())
                return false;
            for (int i = 0; i < size; i++) {
                SvrNodeInfo info1 = this.nodes.get(i);
                SvrNodeInfo info2 = other.nodes.get(i);
                if (info1 == null && info2 != null)
                    return false;
                if (!info1.equals(info2))
                    return false;
            }
        } else {
            if (this.nodes != other.nodes)
                return false;
        }
        return true;
    }

}
