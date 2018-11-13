package de.neofonie.osm;

import de.neofonie.osm.reader.OsmReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@Service
public class ImportApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ImportApplication.class);
    @Autowired
    private OsmReaderService osmReaderService;

    @Override
    public void run(String... args) throws Exception {

        Arrays.asList(args)
                .stream()
                .filter(i -> i.startsWith("-importPbf="))
                .map(i -> i.substring("-importPbf=".length()))
                .findAny()
                .ifPresent(f -> {
                    try {
                        osmReaderService.read(new File(f));
                    } catch (IOException e) {
                        throw new IllegalArgumentException("", e);
                    }
                });
    }
}