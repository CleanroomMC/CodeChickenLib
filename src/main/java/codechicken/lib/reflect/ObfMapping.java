package codechicken.lib.reflect;

import codechicken.lib.CodeChickenLib;
import codechicken.lib.internal.CCLLog;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.common.launcher.FMLTweaker;
import net.minecraftforge.fml.relauncher.CoreModManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.commons.Remapper;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ObfMapping {

    public static class ObfRemapper extends Remapper {

        private final HashMap<String, String> fields = new HashMap<>();
        private final HashMap<String, String> funcs = new HashMap<>();

        @SuppressWarnings ("unchecked")
        public ObfRemapper() {

            try {
                Map<String, Map<String, String>> rawFieldMaps = FMLDeobfuscatingRemapper.INSTANCE.getRawFieldMaps();
                Map<String, Map<String, String>> rawMethodMaps = FMLDeobfuscatingRemapper.INSTANCE.getRawMethodMaps();

                if (rawFieldMaps == null) {
                    throw new IllegalStateException("codechicken.lib.asm.ObfMapping loaded too early. Make sure all references are in or after the asm transformer load stage");
                }

                for (Map<String, String> map : rawFieldMaps.values()) {
                    for (Entry<String, String> entry : map.entrySet()) {
                        if (entry.getValue().startsWith("field")) {
                            fields.put(entry.getValue(), entry.getKey().substring(0, entry.getKey().indexOf(':')));
                        }
                    }
                }
                for (Map<String, String> map : rawMethodMaps.values()) {
                    for (Entry<String, String> entry : map.entrySet()) {
                        if (entry.getValue().startsWith("func")) {
                            funcs.put(entry.getValue(), entry.getKey().substring(0, entry.getKey().indexOf('(')));
                        }
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String mapMethodName(String owner, String name, String desc) {

            String s = funcs.get(name);
            return s == null ? name : s;
        }

        @Override
        public String mapFieldName(String owner, String name, String desc) {

            String s = fields.get(name);
            return s == null ? name : s;
        }

        @Override
        public String map(String typeName) {

            return FMLDeobfuscatingRemapper.INSTANCE.unmap(typeName);
        }

        public String unmap(String typeName) {

            return FMLDeobfuscatingRemapper.INSTANCE.map(typeName);
        }

        public boolean isObf(String typeName) {

            return !map(typeName).equals(typeName) || !unmap(typeName).equals(typeName);
        }
    }

    public static class MCPRemapper extends Remapper implements LineProcessor<Void> {

        public static File[] getConfFiles() {

            File notchSrg;
            File csvDir;
            File mappings = new File(Launch.minecraftHome, "mappings");
            File mcpSrg = new File(Launch.minecraftHome, ".gradle/unimined/local/mappings/srg2mcp.tsrg");
            // check for GradleStart system vars
            String notchSrgPath = System.getProperty("net.minecraftforge.gradle.GradleStart.srg.notch-srg");
            String csvDirPath = System.getProperty("net.minecraftforge.gradle.GradleStart.csvDir");

            if (notchSrgPath != null) {
                notchSrg = new File(notchSrgPath);
            } else {
                mappings.mkdir();
                notchSrg = new File(mappings, "deobf_data-1.12.2.tsrg");
                if (!notchSrg.exists()) {
                    try {
                        JarFile universalJar = new JarFile(new File(FMLTweaker.getJarLocation()));
                        JarEntry entry = universalJar.getJarEntry("deobf_data-1.12.2.tsrg");
                        IOUtils.copy(universalJar.getInputStream(entry), new FileOutputStream(notchSrg));
                        universalJar.close();
                    } catch (IOException e) {
                        CCLLog.logger.fatal("Failed to get mapping file from universal jar.", e);
                    }
                }
            }

            if (csvDirPath != null && !mcpSrg.exists()) {
                csvDir = new File(csvDirPath);
            } else {
                mappings.mkdir();
                csvDir = mappings;
                File mappingZip = new File(mappings, "mcp_stable-39-1.12.zip");
                try {
                    if (!(mappingZip.exists() && DigestUtils.sha256Hex(new FileInputStream(mappingZip))
                                    .equals("13a31f28c11f8f395ffe7e8563ade459f5a0ee46493abbbde3ce6e9493ac4152"))) {
                        FileUtils.copyURLToFile(new URI("https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable/39-1.12/mcp_stable-39-1.12.zip").toURL(), mappingZip);
                    }
                    try (ZipFile zipFile = new ZipFile(mappingZip)) {
                        Enumeration<? extends ZipEntry> entries = zipFile.entries();
                        while (entries.hasMoreElements()) {
                            ZipEntry entry = entries.nextElement();
                            File entryDestination = new File(mappings, entry.getName());
                            try (InputStream in = zipFile.getInputStream(entry);
                                 OutputStream out = Files.newOutputStream(entryDestination.toPath())
                            ) {
                                IOUtils.copy(in, out);
                            }

                        }
                    }

                } catch (URISyntaxException | IOException e) {
                    CCLLog.logger.fatal("Failed to download mcp mapping file.", e);
                }
            }

            if (notchSrg.exists() && csvDir.exists()) {
                File fieldCsv = new File(csvDir, "fields.csv");
                File methodCsv = new File(csvDir, "methods.csv");

                if (notchSrg.exists() && fieldCsv.exists() && methodCsv.exists()) {
                    return new File[] { notchSrg, fieldCsv, methodCsv };
                }
            } else if (notchSrg.exists() && mcpSrg.exists()) {
                return new File[] { notchSrg, mcpSrg };
            }

            throw new RuntimeException("Failed to grab mappings from GradleStart args.");
        }

        private final HashMap<String, String> fields = new HashMap<>();
        private final HashMap<String, String> funcs = new HashMap<>();

        public MCPRemapper() {

            File[] mappings = getConfFiles();
            if (mappings.length == 3) {
                try {
                    Resources.readLines(mappings[1].toURI().toURL(), StandardCharsets.UTF_8, this);
                    Resources.readLines(mappings[2].toURI().toURL(), StandardCharsets.UTF_8, this);
                } catch (IOException e) {
                    CCLLog.logger.fatal("Failed to read mapping csv files.");
                }
            } else {
                try {
                    Resources.readLines(mappings[1].toURI().toURL(), StandardCharsets.UTF_8, this);
                } catch (IOException e) {
                    CCLLog.logger.fatal("Failed to read mapping tsrg files.");
                }
            }
        }

        @Override
        public String mapMethodName(String owner, String name, String desc) {

            String s = funcs.get(name);
            return s == null ? name : s;
        }

        @Override
        public String mapFieldName(String owner, String name, String desc) {

            String s = fields.get(name);
            return s == null ? name : s;
        }

        @Override
        public boolean processLine(@Nonnull String line) throws IOException {
            if (line.contains(",")) {
                int i = line.indexOf(',');
                String srg = line.substring(0, i);
                int i2 = i + 1;
                i = line.indexOf(',', i2);
                String mcp = line.substring(i2, i);
                (srg.startsWith("func") ? funcs : fields).put(srg, mcp);
            } else {
                if (line.startsWith("\t")) {
                    String[] values = line.substring(1).split(" ");
                    if (values.length == 3) {
                        funcs.put(values[0], values[2]);
                    } else if (values.length == 2) {
                        fields.put(values[0], values[1]);
                    }
                }
            }
            return true;
        }

        @Override
        public Void getResult() {

            return null;
        }
    }

    public static ObfRemapper obfMapper = new ObfRemapper();
    public static Remapper mcpMapper = null;

    public static void loadMCPRemapper() {

        if (mcpMapper == null) {
            mcpMapper = new MCPRemapper();
        }
    }

    public static final boolean obfuscated;

    static {
        obfuscated = !(boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
    }

    public static void init() {
        if (!obfuscated) {
            loadMCPRemapper();
        }
    }

    public String s_owner;
    public String s_name;
    public String s_desc;

    public ObfMapping(String owner) {

        this(owner, "", "");
    }

    public ObfMapping(String owner, String name) {

        this(owner, name, "");
    }

    public ObfMapping(String owner, String name, String desc) {

        this.s_owner = owner;
        this.s_name = name;
        this.s_desc = desc;

        if (s_owner.contains(".")) {
            throw new IllegalArgumentException(s_owner);
        }
    }

    public ObfMapping(ObfMapping descmap, String subclass) {

        this(subclass, descmap.s_name, descmap.s_desc);
    }

    public static ObfMapping fromDesc(String s) {

        int lastDot = s.lastIndexOf('.');
        if (lastDot < 0) {
            return new ObfMapping(s, "", "");
        }
        int sep = s.indexOf('(');//methods
        int sep_end = sep;
        if (sep < 0) {
            sep = s.indexOf(' ');//some stuffs
            sep_end = sep + 1;
        }
        if (sep < 0) {
            sep = s.indexOf(':');//fields
            sep_end = sep + 1;
        }
        if (sep < 0) {
            return new ObfMapping(s.substring(0, lastDot), s.substring(lastDot + 1), "");
        }

        return new ObfMapping(s.substring(0, lastDot), s.substring(lastDot + 1, sep), s.substring(sep_end));
    }

    public ObfMapping subclass(String subclass) {

        return new ObfMapping(this, subclass);
    }

    public boolean isClass(String name) {

        return name.replace('.', '/').equals(s_owner);
    }

    public boolean matches(String name, String desc) {

        return s_name.equals(name) && s_desc.equals(desc);
    }

    public String javaClass() {

        return s_owner.replace('/', '.');
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof ObfMapping desc)) {
            return false;
        }

        return s_owner.equals(desc.s_owner) && s_name.equals(desc.s_name) && s_desc.equals(desc.s_desc);
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(s_desc, s_name, s_owner);
    }

    @Override
    public String toString() {

        if (s_name.isEmpty()) {
            return "[" + s_owner + "]";
        }
        if (s_desc.isEmpty()) {
            return "[" + s_owner + "." + s_name + "]";
        }
        return "[" + (isMethod() ? methodDesc() : fieldDesc()) + "]";
    }

    public String methodDesc() {

        return s_owner + "." + s_name + s_desc;
    }

    public String fieldDesc() {

        return s_owner + "." + s_name + ":" + s_desc;
    }

    public boolean isClass() {

        return s_name.isEmpty();
    }

    public boolean isMethod() {

        return s_desc.contains("(");
    }

    public boolean isField() {

        return !isClass() && !isMethod();
    }

    public ObfMapping map(Remapper mapper) {

        if (mapper == null) {
            return this;
        }

        if (isMethod()) {
            s_name = mapper.mapMethodName(s_owner, s_name, s_desc);
        } else if (isField()) {
            s_name = mapper.mapFieldName(s_owner, s_name, s_desc);
        }

        s_owner = mapper.mapType(s_owner);

        if (isMethod()) {
            s_desc = mapper.mapMethodDesc(s_desc);
        } else if (!s_desc.isEmpty()) {
            s_desc = mapper.mapDesc(s_desc);
        }

        return this;
    }

    public ObfMapping toRuntime() {

        map(mcpMapper);
        return this;
    }

    public ObfMapping toClassloading() {

        if (!obfuscated) {
            map(mcpMapper);
        } else if (obfMapper.isObf(s_owner)) {
            map(obfMapper);
        }
        return this;
    }

    public ObfMapping copy() {

        return new ObfMapping(s_owner, s_name, s_desc);
    }
}
