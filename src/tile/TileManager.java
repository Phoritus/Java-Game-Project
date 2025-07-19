package src.tile;

import src.main.GamePanel;
import java.awt.Graphics2D;

import javax.imageio.ImageIO;

import java.io.BufferedReader;
import java.io.InputStream;


public class TileManager {
    GamePanel gp; // Reference to the GamePanel for accessing game settings
    public Tile[] tile; // Array to hold different types of tiles
    public int mapTileNum[][]; // 2D array to hold tile numbers for the map

    public TileManager(GamePanel gp) {
        this.gp = gp; // Initialize the GamePanel reference

        tile = new Tile[50]; // Increase array size to accommodate more tiles
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow]; // Initialize the map tile number array

        getTileImage(); // Load tile images

        // Additional initialization code can go here if needed
        loadMap("/res/maps/worldV2.txt"); // Load the new map
    }

    public void loadMap(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            BufferedReader br = new BufferedReader(new java.io.InputStreamReader(is));

            int worldCol = 0;
            int worldRow = 0;

            while (worldRow < gp.maxWorldRow) {
                String line = br.readLine();
                if (line == null) break; // End of file
                
                String[] numbers = line.split(" ");
                
                worldCol = 0;
                while (worldCol < gp.maxWorldCol) {
                    int num = Integer.parseInt(numbers[worldCol]); // Parse the tile number
                    mapTileNum[worldCol][worldRow] = num; // Store the tile number in the map array
                    worldCol++;
                }
                worldRow++;
            }

            br.close();

        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions, such as file not found
        }
    }

    public void getTileImage() {
        try {
            
            // Placeholder for tile images
            setup(0, "/res/tiles/grass01.png", false);
            setup(1, "/res/tiles/grass01.png", false);
            setup(2, "/res/tiles/grass01.png", false);
            setup(3, "/res/tiles/grass01.png", false);
            setup(4, "/res/tiles/grass01.png", false);
            setup(5, "/res/tiles/grass01.png", false);
            setup(6, "/res/tiles/grass01.png", false);
            setup(7, "/res/tiles/grass01.png", false);
            setup(8, "/res/tiles/grass01.png", false);
            setup(9, "/res/tiles/grass01.png", false);
            setup(10, "/res/tiles/grass01.png", false);


            setup(11, "/res/tiles/grass01.png", false);
            // Water wave
            setupWaterAnimation(12);
            setupWaterAnimation(13);
            setup(14, "/res/tiles/water02.png", true);
            setup(15, "/res/tiles/water03.png", true);
            setup(16, "/res/tiles/water04.png", true);
            setup(17, "/res/tiles/water05.png", true);
            setup(18, "/res/tiles/water06.png", true);
            setup(19, "/res/tiles/water07.png", true);
            setup(20, "/res/tiles/water08.png", true);
            setup(21, "/res/tiles/water09.png", true);
            setup(22, "/res/tiles/water10.png", true);
            setup(23, "/res/tiles/water11.png", true);
            setup(24, "/res/tiles/water12.png", true);
            setup(25, "/res/tiles/water13.png", true);
            setup(26, "/res/tiles/road00.png", false);
            setup(27, "/res/tiles/road01.png", false);
            setup(28, "/res/tiles/road02.png", false);
            setup(29, "/res/tiles/road03.png", false);
            setup(30, "/res/tiles/road04.png", false);
            setup(31, "/res/tiles/road05.png", false);
            setup(32, "/res/tiles/road06.png", false);
            setup(33, "/res/tiles/road07.png", false);
            setup(34, "/res/tiles/road08.png", false);
            setup(35, "/res/tiles/road09.png", false);
            setup(36, "/res/tiles/road10.png", false);
            setup(37, "/res/tiles/road11.png", false);
            setup(38, "/res/tiles/road12.png", false);
            setup(39, "/res/tiles/earth.png", false);
            setup(40, "/res/tiles/wall.png", true);
            setup(41, "/res/tiles/tree.png", true);
            setup(42, "/res/tiles/water00.png", true);
            setup(43, "/res/tiles/lava_floor.png", false);
            setup(44, "/res/tiles/right.png", false);

        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions, such as file not found
        }
    }

    public void setup(int index, String imagePath, boolean collision) {
        try {
            tile[index] = new Tile();
            tile[index].image = ImageIO.read(getClass().getResourceAsStream(imagePath)); // Load the tile image
            
            // Scale the image to the tile size
            java.awt.Image scaledImage = tile[index].image.getScaledInstance(gp.tileSize, gp.tileSize, java.awt.Image.SCALE_SMOOTH);
            tile[index].image = new java.awt.image.BufferedImage(gp.tileSize, gp.tileSize, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            tile[index].image.getGraphics().drawImage(scaledImage, 0, 0, null);
            
            tile[index].collision = collision; // Set collision property
        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions, such as file not found
        }
    }

    public void setupWaterAnimation(int index) {
        try {
            // Load the 3 water animation frames
            java.awt.image.BufferedImage[] waterFrames = new java.awt.image.BufferedImage[3];
            
            waterFrames[0] = ImageIO.read(getClass().getResourceAsStream("/res/tiles/water/w1.png"));
            waterFrames[1] = ImageIO.read(getClass().getResourceAsStream("/res/tiles/water/w2.png"));
            waterFrames[2] = ImageIO.read(getClass().getResourceAsStream("/res/tiles/water/w3.png"));
            
            // Scale all frames to tile size
            for (int i = 0; i < waterFrames.length; i++) {
                java.awt.Image scaledImage = waterFrames[i].getScaledInstance(gp.tileSize, gp.tileSize, java.awt.Image.SCALE_SMOOTH);
                waterFrames[i] = new java.awt.image.BufferedImage(gp.tileSize, gp.tileSize, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                waterFrames[i].getGraphics().drawImage(scaledImage, 0, 0, null);
            }
            
            // Create animated tile with very slow animation (30 frame delay for slower effect)
            tile[index] = new Tile(waterFrames, 30);
            tile[index].collision = false; // Water has no collision
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isTileCollision(int tileNum) {
        if (tileNum >= 0 && tileNum < tile.length && tile[tileNum] != null) {
            return tile[tileNum].collision;
        }
        return false;
    }

    public void draw(Graphics2D g2) {
        int worldCol = 0;
        int worldRow = 0;

        while (worldCol < gp.maxWorldCol && worldRow < gp.maxWorldRow) {

            int tileNum = mapTileNum[worldCol][worldRow]; // Get the tile number for the current position
            int worldX = worldCol * gp.tileSize; // Calculate world X position
            int worldY = worldRow * gp.tileSize; // Calculate world Y position
            int screenX = worldX - gp.player.worldX + gp.player.screenX; // Calculate screen X position
            int screenY = worldY - gp.player.worldY + gp.player.screenY; // Calculate screen Y position

            // Only draw tiles that are visible on screen for performance
            if (screenX > -gp.tileSize && screenX < gp.screenWidth && 
                screenY > -gp.tileSize && screenY < gp.screenHeight) {
                
                if (tileNum >= 0 && tileNum < tile.length && tile[tileNum] != null) {
                    // Don't update animation here - it's handled in update() method
                    
                    // If it's a tree tile (tile 4), draw grass underneath first
                    if (tileNum == 4) {
                        g2.drawImage(tile[0].image, screenX, screenY, null); // Draw grass first
                        g2.drawImage(tile[tileNum].getCurrentFrame(), screenX, screenY, null); // Then draw tree on top
                    } else {
                        g2.drawImage(tile[tileNum].getCurrentFrame(), screenX, screenY, null); // Draw the tile image
                    }
                }
            }
            
            worldCol++;

            if (worldCol == gp.maxWorldCol) {
                worldCol = 0;
                worldRow++;
            }
        }
    }

    public void update() {
        // Update animation for all tiles once per frame
        for (int i = 0; i < tile.length; i++) {
            if (tile[i] != null && tile[i].hasAnimation) {
                tile[i].updateAnimation();
            }
        }
    }

}
