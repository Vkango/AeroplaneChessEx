package bootstrap.spi;

import plugin.api.IMapProvider;
import plugin.api.IRuleSetProvider;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class PluginLoader {

    private static PluginLoader instance;
    private final Map<String, IMapProvider> mapProviders = new HashMap<>();
    private final Map<String, IRuleSetProvider> ruleSetProviders = new HashMap<>();
    private final List<URLClassLoader> classLoaders = new ArrayList<>();

    private PluginLoader() {
    }

    public static synchronized PluginLoader getInstance() {
        if (instance == null) {
            instance = new PluginLoader();
        }
        return instance;
    }

    public void loadPluginsFromJars(List<String> jarPaths) throws Exception {
        if (jarPaths == null || jarPaths.isEmpty()) {
            System.out.println("[INFO] 没有指定插件JAR文件");
            return;
        }

        List<URL> urls = new ArrayList<>();

        for (String jarPath : jarPaths) {
            File jarFile = new File(jarPath);
            if (!jarFile.exists()) {
                System.err.println("[WARN] 插件JAR文件不存在: " + jarPath);
                continue;
            }

            if (!jarFile.getName().endsWith(".jar")) {
                System.err.println("[WARN] 不是有效的JAR文件: " + jarPath);
                continue;
            }

            urls.add(jarFile.toURI().toURL());
            System.out.println("[INFO] 发现插件JAR: " + jarPath);
        }

        if (urls.isEmpty()) {
            System.out.println("[INFO] 没有有效的插件JAR文件");
            return;
        }

        // 创建类加载器
        URLClassLoader classLoader = new URLClassLoader(
                urls.toArray(new URL[0]),
                Thread.currentThread().getContextClassLoader());
        classLoaders.add(classLoader);

        ServiceLoader<IMapProvider> mapLoader = ServiceLoader.load(IMapProvider.class, classLoader);
        for (IMapProvider provider : mapLoader) {
            String name = provider.getMapName().toLowerCase();
            mapProviders.put(name, provider);
            System.out.println("[OK] 加载地图插件: " + provider.getMapName() +
                    " (v" + provider.getVersion() + ") by " + provider.getAuthor());
        }

        ServiceLoader<IRuleSetProvider> ruleLoader = ServiceLoader.load(IRuleSetProvider.class, classLoader);
        for (IRuleSetProvider provider : ruleLoader) {
            String name = provider.getRuleSetName().toLowerCase();
            ruleSetProviders.put(name, provider);
            System.out.println("[OK] 加载规则插件: " + provider.getRuleSetName() +
                    " (v" + provider.getVersion() + ") by " + provider.getAuthor());
        }

        if (mapProviders.isEmpty() && ruleSetProviders.isEmpty()) {
            System.out.println("[INFO] JAR中未找到插件");
        }
    }

    public IMapProvider getMapProvider(String name) {
        IMapProvider provider = mapProviders.get(name.toLowerCase());
        if (provider == null) {
            throw new RuntimeException("找不到地图: " + name +
                    "\n可用的地图: " + String.join(", ", mapProviders.keySet()));
        }
        return provider;
    }

    public IRuleSetProvider getRuleSetProvider(String name) {
        IRuleSetProvider provider = ruleSetProviders.get(name.toLowerCase());
        if (provider == null) {
            throw new RuntimeException("找不到规则集: " + name +
                    "\n可用的规则集: " + String.join(", ", ruleSetProviders.keySet()));
        }
        return provider;
    }

    public Set<String> getAvailableMaps() {
        return new HashSet<>(mapProviders.keySet());
    }

    public Set<String> getAvailableRuleSets() {
        return new HashSet<>(ruleSetProviders.keySet());
    }

    public void cleanup() {
        for (URLClassLoader loader : classLoaders) {
            try {
                loader.close();
            } catch (Exception e) {
                System.err.println("[ERROR] 关闭类加载器失败: " + e.getMessage());
            }
        }
        classLoaders.clear();
        mapProviders.clear();
        ruleSetProviders.clear();
    }
}
