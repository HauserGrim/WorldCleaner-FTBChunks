package worldcleaner.providers;

import worldcleaner.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;

public class FTBChunksProvider extends DataProvider{

    public FTBChunksProvider(String worldName, String dimensionName) throws Throwable {
        super(worldName, dimensionName);
    }

    @Override
    protected void init() throws Throwable {
        for (File dataFile : FileUtils.safeListFiles(Paths.get("").resolve(worldName).resolve("ftbchunks").toFile())) {
            if (dataFile.getName().endsWith(".snbt")) {
                // Костылим, так как либы читающие snbt не читали использовавшие майновский файлы.
                // Мб есть варианты лучше, но пока и так вроде работает.
                try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
                    String line;
                    boolean dim = false;
                    while ((line = br.readLine()) != null) {
                        if (dim) {
                            if (line.contains("]")) break;
                            String x = line;
                            x = x.substring(x.indexOf("x: ") + 3);
                            x = x.substring(0, x.indexOf(", z"));
                            String z = line;
                            z = z.substring(z.indexOf("z: ") + 3);
                            z = z.substring(0, z.indexOf(", t"));
                            addChunkAtCoord(Integer.parseInt(x), Integer.parseInt(z));
                        }
                        if (line.contains(dimensionName)) dim = true;
                    }
                }
            }
        }
    }

}
