package com.moguhu.zuul.zookeeper.curator;

import com.moguhu.zuul.constants.ZuulConstants;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Curator client
 *
 * @author xuefeihu
 */
public class CuratorClient {

    protected static final Logger logger = LoggerFactory.getLogger(CuratorClient.class);

    private static CuratorClient INSTANCE = null;

    public static final String CHARSET = "UTF-8";
    private CuratorFramework client;

    private static final DynamicStringProperty hosts = DynamicPropertyFactory.getInstance()
            .getStringProperty(ZuulConstants.BAIZE_ZOOKEEPER_HOSTS, null);

    public static CuratorClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CuratorClient();
        }
        return INSTANCE;
    }

    /**
     * create client
     */
    private CuratorClient() {
        if (StringUtils.isEmpty(hosts.get())) {
            logger.error("the property of 'baize.zookeeper.hosts' is empty! System out.");
            System.exit(-1);
        }

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
        this.client = CuratorFrameworkFactory.builder().connectString(hosts.get()).sessionTimeoutMs(10000)
                .retryPolicy(retryPolicy).build();
        client.start();
    }

    /**
     * create node
     *
     * @param path
     * @param data
     * @param createMode
     */
    public void createNode(String path, String data, CreateMode createMode) {
        try {
            client.create().creatingParentsIfNeeded()
                    .withMode(createMode).withACL(Ids.OPEN_ACL_UNSAFE)
                    .forPath(path, data.getBytes(CHARSET));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * delete node
     *
     * @param path
     */
    public void deleteNode(String path) {
        try {
            client.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * read node
     *
     * @param path
     * @return
     */
    public String readNode(String path) {
        Stat stat = new Stat();
        return this.readNode(path, stat);
    }

    /**
     * read node and stat
     *
     * @param path
     * @return
     */
    public String readNode(String path, Stat stat) {
        String result = "";
        try {
            byte[] data = client.getData().storingStatIn(stat).forPath(path);
            result = new String(data, CHARSET);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * update node
     *
     * @param path
     * @param data
     * @param version
     */
    public void updateNode(String path, String data, int version) {
        try {
            client.setData().withVersion(version).forPath(path, data.getBytes(CHARSET));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get children
     *
     * @param path
     * @return
     */
    public List<String> getChildren(String path) {
        Stat stat = new Stat();
        return this.getChildren(path, stat);
    }

    /**
     * get children and store stat
     *
     * @param path
     * @param stat
     * @return
     */
    public List<String> getChildren(String path, Stat stat) {
        List<String> result = new ArrayList<String>();
        try {
            result = client.getChildren().storingStatIn(stat).forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * check exists
     *
     * @param path
     * @return
     */
    public boolean checkExists(String path) {
        boolean result = false;
        try {
            result = client.checkExists().forPath(path) != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void closeClient() {
        if (null != client)
            this.client.close();
    }

    public CuratorFramework getClient() {
        return client;
    }

    public void setClient(CuratorFramework client) {
        this.client = client;
    }

}
