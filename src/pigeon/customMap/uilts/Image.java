package pigeon.customMap.uilts;

import pigeon.customMap.Main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

@SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
public class Image {

    public static BufferedImage[] splitImage(String name, int rows, int cols) throws IOException {
        String originalImg = Main.getPlugin().getDataFolder() + "/images/" + name;
        File file = new File(originalImg);
        FileInputStream fis = new FileInputStream(file);
        BufferedImage image = ImageIO.read(fis);
        int chunks = rows * cols;
        int chunkWidth = image.getWidth() / cols;
        int chunkHeight = image.getHeight() / rows;
        int count = 0;
        BufferedImage[] imgs = new BufferedImage[chunks];
        for(int x = 0; x < rows; ++x) {
            for(int y = 0; y < cols; ++y) {
                imgs[count] = new BufferedImage(chunkWidth, chunkHeight, image.getType());
                Graphics2D gr = imgs[count++].createGraphics();
                gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
                gr.dispose();
            }
        }
        return imgs;
    }

    public static String ext(String filename) {
        int index = filename.lastIndexOf(".");
        if (index == -1) {
            return null;
        } else {
            return filename.substring(index + 1);
        }
    }

    public static String name(String filename) {
        return filename.replaceAll("." + ext(filename), "");
    }

    public static void download(String urlString, String filename, String savePath) throws Exception {
        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
        con.setConnectTimeout(5000);
        InputStream is = con.getInputStream();
        byte[] bs = new byte[1024];
        File sf = new File(savePath);
        if (!sf.exists()) {
            sf.mkdirs();
        }
        FileOutputStream os = new FileOutputStream(sf.getPath() + "\\" + filename);
        int len;
        while((len = is.read(bs)) != -1) {
            os.write(bs, 0, len);
        }
        os.close();
        is.close();
    }
}
