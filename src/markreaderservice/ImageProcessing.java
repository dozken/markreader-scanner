package markreaderservice;

/**
 *
 * @author technovision, yerzhan mukhamedzhan, dosmukhamed zhanibekov, Bakhytzhan Kadyrov
 */
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;

public class ImageProcessing {

    ImageProcessing imageProcessing;
    String fileName = "";

    public ImageProcessing() {
        imageProcessing = this;
    }

    public void watchDirectoryPath(Path path) {
        // Sanity check - Check if path is a folder
        try {
            Boolean isFolder = (Boolean) Files.getAttribute(path,
                    "basic:isDirectory", NOFOLLOW_LINKS);
            if (!isFolder) {
                throw new IllegalArgumentException("Path: " + path + " is not a folder");
            }
        } catch (IOException ioe) {
            // Folder does not exists
            ioe.printStackTrace();
        }

        System.out.println("Watching path: " + path);

        // We obtain the file system of the Path
        FileSystem fs = path.getFileSystem();

        // We create the new WatchService using the new try() block
        try (WatchService service = fs.newWatchService()) {

            // We register the path to the service
            // We watch for creation events
            path.register(service, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);

            // Start the infinite polling loop
            WatchKey key = null;
            while (true) {
                key = service.take();

                // Dequeueing events
                Kind<?> kind = null;
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    // Get the type of the event
                    kind = watchEvent.kind();
                    if (OVERFLOW == kind) {
                        continue; //loop
                    } else if (ENTRY_CREATE == kind) {
                        // A new Path was created 
                        
                        Path newPath = ((WatchEvent<Path>) watchEvent).context();
                        Path dir = (Path)key.watchable();
                        Path fullPath = dir.resolve(((WatchEvent<Path>) watchEvent).context());
                        File file = fullPath.toFile();
                        // Output
                        System.out.println("ENTRY_CREATE: " + fullPath.toString());
                        //Thread.sleep(5000);
                        process(file);
                    }

                }

                if (!key.reset()) {
                    break; //loop
                }
            }

        } catch (IOException | InterruptedException ioe) {
            ioe.printStackTrace();
        }

    }

    public BufferedImage toGrayscale(File file) {
        BufferedImage img = null; 
        fileName = file.getName();
        try {
            if (file.exists()) {
                System.out.println(file.getAbsolutePath());
            } else {
                System.out.println("supeer");
            }
            img = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
       
        int width = img.getWidth();
        int height = img.getHeight();
           
        int c = 120;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {              
                int p = img.getRGB(x, y);
                int a = (p >> 24) & 0xff;
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;

                if ((r > c)) {
                    p = 0xffffffff;
                }
                img.setRGB(x, y, p);
            }
        }
        
        
            if(width<height){img= rotate90ToLeft(img);}
            double angle=0;
            int matrix[][];
            //img =rotate(img);
            //matrix=getMatrix(img);
            //angle=findTwoCordinates(matrix);
            //img =rotateToAngle(img,angle);            
        
            return img;
    }
    
    public BufferedImage rotate(BufferedImage img) {
        BufferedImage image = img;        
        int angle=0;     
        int[][] matrix =getMatrix(image);
        angle=getAngle(matrix);
        if(angle<100){image=rotate180(image);}
                 
        return image;
    }
    
    public BufferedImage rotate180( BufferedImage inputImage ) {
	int width = inputImage.getWidth(); 
	int height = inputImage.getHeight();
	BufferedImage returnImage = new BufferedImage( width, height, inputImage.getType()  );

	for( int x = 0; x < width; x++ ) {
		for( int y = 0; y < height; y++ ) {
	               returnImage.setRGB(width-x-1, height-y-1, inputImage.getRGB(x, y));
		}
	}
	return returnImage;
    }
    
    public BufferedImage rotate90ToLeft( BufferedImage inputImage ){
	int width = inputImage.getWidth();
	int height = inputImage.getHeight();
	BufferedImage returnImage = new BufferedImage( height, width , inputImage.getType()  );
             
	for( int x = 0; x < width; x++ ) {
		for( int y = 0; y < height; y++ ) {
			
                        returnImage.setRGB(y, width-x-1,inputImage.getRGB(x, y));
		}
	}
	return returnImage;
}
    
    public int[][] getMatrix(BufferedImage img){
        
        BufferedImage image=img;
        int[][] matrix = new int[image.getHeight()][image.getWidth()];
           
            for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                int clr = image.getRGB(j, i);
                int blue = clr & 0xFF;
                if (blue > 200) {
                    matrix[i][j] = 0;
                } else if (blue <= 200) {
                    matrix[i][j] = 1;
                }
            }
        }
        return matrix;
    }
    
    public int getAngle(int array[][]){
        
        int count=0;
        int countAll=0;
        int matrix[][]=array;
        
             
        for(int row=0;row<250;row++){
            for(int column=0;column<250;column++){
                if(matrix[row][column]==1){
                    
                    for(int total=column;total<column+20;total++){
                        if(matrix[row+2][total]==1){
                           count++; 
                           
                        }
                    }
                }
                
                if(count>16){
                    for(int total=column;total<column+350;total=total+50){
                        for(int a=total;a<total+19;a++){                         
                            if(matrix[row+2][a]==1){
                            countAll++; 
                          
                                    
                            }
                        }
                        
                    }
                
                }
                
                if(countAll>100)break;
                else {count=0;countAll=0;}
            }
            if(countAll>100)break;{count=0;countAll=0;}
        }
        
        return countAll;
    }
    
    public double findTwoCordinates(int[][] array){
        
        int matrix[][]=array;
        int x=0;
        int y=0;
        int x1=0;
        int y1=0;
        int count=0;
        
        for(int row=0;row<250;row++){//150
            count=0;
            for(int column=0;column<200;column++){//200
                if(matrix[row][column]==1){
                    
                    for(int total=column;total<column+20;total++){
                        if(matrix[row+2][total]==1){
                           count++; 
                           
                        }
                    }
                }
                
                if(count>16){
                     x=row;
                     y=column;break;
                }
                
                
            }
           if(count>16){count=0;break;}
        }
            
            
        for(int row=0;row<250;row++){//150
            count=0;
            for(int column=matrix[row].length-1;column>matrix[row].length-200;column--){//200
                if(matrix[row][column]==1){
                    
                    for(int total=column;total>column-20;total--){
                        if(matrix[row+2][total]==1){
                           count++; 
                           
                        }
                    }
                }
                
                if(count>16){
                     x1=row;
                     y1=column;break;
                }
                
                
            }
           if(count>16){count=0;break;}
        }
        
        System.out.println(x+":"+y);
        System.out.println(x1+":"+y1);
        
        double angle=0.0;
        if(x!=x1){
        angle=Math.atan(Math.abs(y-y1)/Math.abs(x-x1));
        angle=90-Math.toDegrees(angle);
        }   
        if(x<x1){angle=angle*(-1);};
        
        return angle;
    }
    
    public BufferedImage rotateToAngle(BufferedImage image, double ang){  
        BufferedImage img  = image;  
        double angle=ang;
        int w=img.getWidth();
        int h=img.getHeight();                       
        BufferedImage dimg =new BufferedImage(w, h, img.getType());
        Graphics2D g = dimg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); 
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);          
        g.rotate(Math.toRadians(angle), w/2, h/2);
        g.drawImage(img, 0, 0, Color.WHITE, null);
    
        return dimg;
    }
        
    public int[][] getNormMatrix(BufferedImage image) {
      
      int[][] matrix = getMatrix(image);
      int correctMatrix[][]=findCircle2(matrix);    
      
      try {
            File f = new File("data/grayscale/gray_"+fileName);
            ImageIO.write(image, "jpg", f);
            System.out.println("Image written");
        } catch (Exception e) {
            System.out.println(e);
        }

        return correctMatrix;
    }

    public int[][] findCircle2(int [][] array){
        int cordinates[]=new int[2];
        cordinates=findCordinate(array);
        cordinates[0]=cordinates[0]+47;
        cordinates[1]=cordinates[1]-16;                
        
        int matrix[][]=array;
        int correctMatrix[][]=new int[45][63];
        int correctRow=0;
        int correctColumn=0;
            for(int row=cordinates[0];row<=cordinates[0]+50*44;row=row+50){  
              for(int column=cordinates[1];column<=cordinates[1]+50*62;column=column+50){        
               correctMatrix[correctRow][correctColumn]=checkCircle(row,column,matrix);
               correctColumn++;
             }     
            correctColumn=0;
            correctRow++;
        }
        return correctMatrix;                    
    }
    
    public int[] findCordinate(int[][] array){
        
        int count=0;
        int matrix[][]=array;
        int cordinates[]=new int[2];
        for(int row=0;row<200;row++){
            for(int column=0;column<200;column++){
                if(matrix[row][column]==1){
                    for(int total=column;total<column+20;total++){
                        if(matrix[row][total]==1){
                           count++; 
                        }
                    }
                }
                if(count>13){cordinates[0]=row;cordinates[1]=column;break;}
                else {count=0;}
            }
            if(cordinates[0]!=0)break;
        }
        
        return cordinates;
    }
   
    public int checkCircle(int a,int b,int array[][]){
       
        int count=0;
        int matrix[][]=array;
        
        for(int row=a;row<a+50;row++){       
            if(row<matrix.length){
                for(int column=b;column<b+50;column++){  
                     if(column<matrix[row].length && column>=0){
                        if(matrix[row][column]==1){
                            count++; 
                        }               
                      }
                }
            }    
        }
   
         if(count>400){
             return 1;
         }else return 0;
    }
    
    public void saveToTxt(int [][]matrix,File file) {
        PrintWriter out = null;
        try {
            String text = java.util.Arrays.deepToString(matrix).replace("], ", "\n").replaceAll("[\\[,\\]]", "");
        
            String dirPath = "data/txt/";
          
            out = new PrintWriter(dirPath+file.getName()+".txt");
            out.println(text);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ImageProcessing.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }
    }

    public void process(File file) {
        String mimetype = new MimetypesFileTypeMap().getContentType(file);
        String type = mimetype.split("/")[0];
        int[][] matrix = null;
        if (type.equals("image")) {
            BufferedImage grayImg = toGrayscale(file);
            matrix = getNormMatrix(grayImg);
            int newMatrix[][]=new int[63][45];
          /*  for(int row=0;row<matrix.length;row++){
            for(int column=0;column<matrix[row].length;column++){
             
              System.out.print(matrix[row][column]);
            }
             System.out.println();
            }    
             System.out.println();
              System.out.println();
            for(int row=0;row<63;row++){
                
            for(int column=0;column<45;column++){
              newMatrix[row][column]=matrix[44-column][row];
              System.out.print(newMatrix[row][column]);
            }
             System.out.println();
            } */   
        }else {System.out.println("ee bolmady goi: "+type);}
        saveToTxt(matrix,file);
    }
}
