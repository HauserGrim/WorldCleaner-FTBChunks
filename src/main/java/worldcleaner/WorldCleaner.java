package worldcleaner;

import worldcleaner.providers.DataProvider;
import worldcleaner.providers.FTBChunksProvider;
import worldcleaner.storage.AnvilRegion;
import worldcleaner.storage.Coord;
import worldcleaner.storage.WorldMap;
import worldcleaner.utils.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

public class WorldCleaner {
    public static void main(String[] args) {
        FileUtils.init();
        AnvilRegion.init();

        Scanner scanner = new Scanner(System.in);

        print("НЕ ЗАБУДЬТЕ СДЕЛАТЬ БЭКАП!");

        // TODO Выводить список доступных миров. Сканировать по наличию level.dat?
        print("Введите название папки мира:");
        String worldName = scanner.nextLine();

        if (!Paths.get("").resolve(worldName).toFile().exists()) {
            System.out.println("Такой папки не найдено!");
            return;
        }

        // TODO Найти способ сканировать измерения и название их папок.
        print("Измерения:");
        print("1. minecraft:overworld");
        print("2. minecraft:the_nether");
        print("3. minecraft:the_end");
        print("4. модовое");

        print("Введите номер варианта:");
        String dimNumber = scanner.nextLine();;
        switch (dimNumber) {
            case "1" -> doWorldClear(worldName, "minecraft:overworld", "", false);
            case "2" -> doWorldClear(worldName, "minecraft:the_nether", "DIM-1", false);
            case "3" -> doWorldClear(worldName, "minecraft:the_end", "DIM1", false);
            case "4" -> {
                System.out.println("Введите название модового измерения (напр. the_bumblezone:the_bumblezone):");
                String modDimensionName = scanner.nextLine();;
                System.out.println("Введите папку модового измерения (напр. dimensions/twilightforest/twilight_forest):");
                String modDimensionFolder = scanner.nextLine();;
                doWorldClear(worldName, modDimensionName, modDimensionFolder, false);
            }
            default -> System.out.println("Нет варианта с таким номером.");
        }

        scanner.close();
    }

    private static void doWorldClear(String worldName, String dimensionName, String dimensionFolder, boolean poi) {
        final ArrayList<DataProvider> providers = new ArrayList<>();
        Path currentFolder = Paths.get("");

        try {
            if (currentFolder.resolve(worldName).resolve("ftbchunks").toFile().exists()) {
                System.out.println("Данные FTBChunks найдены, добавление в список сохраняемых чанков.");
                providers.add(new FTBChunksProvider(worldName, dimensionName));
            }
            // TODO Можно попробовать добавить другие моды или даже плагины как было в asw
        } catch (Throwable t) {
            System.out.println("Не удалось получить данные FTBChunks.");
            t.printStackTrace();
            return;
        }

        final WorldMap preservechunks = new WorldMap();
        for (DataProvider provider : providers) {
            for (Coord chunkCoord : provider.getChunks()) {
                preservechunks.addChunk(chunkCoord);
            }
        }

        File regionFolder = currentFolder.resolve(worldName).resolve(dimensionFolder).resolve(poi? "poi" : "region").toFile();
        for (File regionfile : FileUtils.safeListFiles(regionFolder)) {
            System.out.println("Обработка файла " + regionfile.getName());
            try {
                AnvilRegion region = new AnvilRegion(regionFolder, regionfile.getName());
                if (preservechunks.hasChunks(region.getX(), region.getZ())) {
                    region.loadFromDisk();
                    Set<Coord> localChunks = preservechunks.getChunks(region.getX(), region.getZ());
                    for (Coord columnchunk : region.getChunks()) {
                        if (!localChunks.contains(columnchunk)) {
                            region.removeChunk(columnchunk);
                        }
                    }
                    region.saveToDisk();
                } else {
                    region.delete();
                }
            } catch (Throwable e) {
                print("Ошибка обработки файла " + regionfile.getName());
                e.printStackTrace();
            }
        }
        if (!poi) {
            print("Region файлы обработаны.");
            print("Приступаем к обработке файлов POI.");
            doWorldClear(worldName, dimensionName, dimensionFolder, true);
        } else {
            print("Файлы POI обработаны.");
        }
    }

    private static void print(String s) {
        System.out.println(s);
    }
}
