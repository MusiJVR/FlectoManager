package org.flectomanager.util;

import javafx.scene.image.Image;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static Image load(String path) {
        try (InputStream svgStream = Utils.class.getClassLoader().getResourceAsStream(path)) {
            if (svgStream == null) throw new FileNotFoundException("SVG file not found: " + path);
            PNGTranscoder transcoder = new PNGTranscoder();
            TranscoderInput input = new TranscoderInput(svgStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outputStream);
            transcoder.transcode(input, output);
            outputStream.flush();
            return new Image(new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (Exception e) {
            log.error("Failed to load SVG file: {}", e.getMessage());
        }
        return null;
    }
}
