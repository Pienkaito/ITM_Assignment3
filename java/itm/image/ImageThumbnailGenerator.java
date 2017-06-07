package itm.image;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/*******************************************************************************
    This file is part of the ITM course 2017
    (c) University of Vienna 2009-2017
*******************************************************************************/

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.imageio.ImageIO;



/**
    This class converts images of various formats to PNG thumbnails files.
    It can be called with 3 parameters, an input filename/directory, an output directory and a compression quality parameter.
    It will read the input image(s), grayscale and scale it/them and convert it/them to a PNG file(s) that is/are written to the output directory.

    If the input file or the output directory do not exist, an exception is thrown.
*/
public class ImageThumbnailGenerator 
{
	public static final Font DEFAULT_FONT = new Font("Arial", Font.BOLD, 100);
    /**
        Constructor.
    */
    public ImageThumbnailGenerator()
    {
    }

    /**
        Processes an image directory in a batch process.
        @param input a reference to the input image file
        @param output a reference to the output directory
        @param overwrite indicates whether existing thumbnails should be overwritten or not
        @return a list of the created files
    */
    public ArrayList<File> batchProcessImages( File input, File output, boolean overwrite ) throws IOException
    {
        if ( ! input.exists() ) {
            throw new IOException( "Input file " + input + " was not found!" );
        }
        if ( ! output.exists() ) {
            throw new IOException( "Output directory " + output + " not found!" );
        }
        if ( ! output.isDirectory() ) {
            throw new IOException( output + " is not a directory!" );
        }

        ArrayList<File> ret = new ArrayList<File>();

        if ( input.isDirectory() ) {
            File[] files = input.listFiles();
            for ( File f : files ) {
                try {
                    File result = processImage( f, output, overwrite );
                    System.out.println( "converted " + f + " to " + result );
                    ret.add( result );
                } catch ( Exception e0 ) {
                    System.err.println( "Error converting " + input + " : " + e0.toString() );
                }
            }
        } else {
            try {
                File result = processImage( input, output, overwrite );
                System.out.println( "converted " + input + " to " + result );
                ret.add( result );
            } catch ( Exception e0 ) {
                System.err.println( "Error converting " + input + " : " + e0.toString() );
            }
        } 
        return ret;
    }  

    /**
        Processes the passed input image and stores it to the output directory.
        This function should not do anything if the outputfile already exists and if the overwrite flag is set to false.
        @param input a reference to the input image file
        @param output a reference to the output directory
        @param dimx the width of the resulting thumbnail
        @param dimy the height of the resulting thumbnail
        @param overwrite indicates whether existing thumbnails should be overwritten or not
    */
    protected File processImage( File input, File output, boolean overwrite ) throws IOException, IllegalArgumentException
    {
        if ( ! input.exists() ) {
            throw new IOException( "Input file " + input + " was not found!" );
        }
        if ( input.isDirectory() ) {
            throw new IOException( "Input file " + input + " is a directory!" );
        }
        if ( ! output.exists() ) {
            throw new IOException( "Output directory " + output + " not found!" );
        }
        if ( ! output.isDirectory() ) {
            throw new IOException( output + " is not a directory!" );
        }

        // create outputfilename and check whether thumb already exists
        File outputFile = new File( output, input.getName() + ".thumb.png" );
        if ( outputFile.exists() ) {
            if ( ! overwrite ) {
                return outputFile;
            }
        }

        // ***************************************************************
        //  Fill in your code here!
        // ***************************************************************

        // load the input image
        
        BufferedImage img = null;
        try {
            img = ImageIO.read(input);
        } catch (IOException e) {
        }
        int todo=0;
        int oheight=img.getHeight();
        int owidth=img.getWidth();
        int nheight=img.getHeight();
        int nwidth=img.getWidth();
        /*
         * Die verschiedenen Fälle in denen das Bild bearbeitet werden muss
         * case1 : Bild muss gedreht werden
         * case2 : add watermark
         * case3 : Bild muss skaliert werden
         * case4 : Bild ist kleiner als 200*100;
         */
        BufferedImage thumb = null;
        //the watermark i wanna add
        BufferedImage scaled = null;
        //im loading the watermark from the internet so i dont have any path problems
        BufferedImage wm = ImageIO.read(new URL("http://i.imgur.com/8XmfJHS.jpg"));
        /*
        try {
             wm = ImageIO.read(new File("media\\watermark\\watermark.jpg"));
         } catch (IOException e) {System.out.println(e);
         }*/
        	if(owidth<oheight){ todo=1;}
        	else
        		todo=2;
        while(todo<6){
        switch(todo){
        	case 1:
        		//das Bild wird gedreht
                nheight=owidth;
                nwidth=oheight;
            	AffineTransform rotator= new AffineTransform();
                thumb= new BufferedImage(nwidth, nheight, BufferedImage.TYPE_INT_RGB);
            	Graphics2D r = thumb.createGraphics();
                rotator.translate((nwidth-owidth)/2,(nheight-oheight)/2);      	
            	rotator.rotate(Math.toRadians(90),owidth/2,oheight/2);
            	r.setTransform(rotator);
            	r.drawRenderedImage(img, null);
            	r.dispose();   	
            	if (thumb.getWidth()>200){todo=3;}
            	else todo=4;
                break;
        	case 2:
        		//das bild wird nicht gedreht
                thumb = img;
                if(thumb.getWidth()>200){todo=3;}else todo=4;
        		break;
        	case 3:
        		//das Bild wird skaliert
        		AffineTransform scale = new AffineTransform();
        		double scaleFactor= (200/(double)thumb.getWidth());
        		scale.scale(scaleFactor, scaleFactor);        		
        		scaled= new BufferedImage((int)(thumb.getWidth()*scaleFactor +0.5),(int)(thumb.getHeight()*scaleFactor +0.5),BufferedImage.TYPE_INT_RGB);
        		Graphics2D s = scaled.createGraphics();
        		s.setTransform(scale);
        		s.drawRenderedImage(thumb, null);
        		s.dispose();
        		thumb=scaled;
        		todo=4;
        		break;
        	case 4:
        		// falls das bild weder im skalierungs noch im drehungsvorgang war(weil es von vorneherein kleiner war wird thumb nun gesetzt
        		if (thumb==null){
        			todo=5;
        			thumb=img;
        			break;
        		}
        		if(thumb.getWidth()<200||thumb.getHeight()<100){
        			todo=5;
        			break;
        		}
        		//jetzt fügen wir die watermark hinzu
        		Graphics2D w = thumb.createGraphics();
        		
        		w.drawRenderedImage(wm, null);
        		w.dispose();
        		todo=6;
        		break;
        	case 5:
        		BufferedImage small= new BufferedImage(200,100, BufferedImage.TYPE_INT_RGB);
        		Graphics2D sm= small.createGraphics();
        		AffineTransform smt = new AffineTransform();
        		smt.translate((200-thumb.getWidth())/2, (100-thumb.getHeight())/2);
        		sm.setTransform(smt);
        		sm.drawRenderedImage(thumb, null);
        		sm.dispose();
        		thumb=small;
        		todo=4;
        		break;
        
        
        }
        }
        try{
        ImageIO.write(thumb, "png", outputFile);
        } catch (IOException e) {       	
        }
        return outputFile;
        
        //below is my first try. above i did it more structured
        /*
        int nheight=img.getHeight();
        int nwidth=img.getWidth();
        int oheight=img.getHeight();
        int owidth=img.getWidth();
        int scalenheight=0;
        int scalenwidth=0;
        int scaleoheight=0;
        int scaleowidth=0;
        if(img.getWidth()<img.getHeight()){
        	nheight=img.getWidth();
        	nwidth=img.getHeight();
        }
        double scale= 1;
        
        if (nwidth>200){
        	scale= (200/(double)nwidth);
        	scalenwidth= (int)(scale * nwidth + 0.5);
        	scalenheight= (int)(scale * nheight + 0.5);
        	scaleowidth= (int)(scale * owidth + 0.5);
        	scaleoheight= (int)(scale * oheight + 0.5);

        }
        //the watermark i will add to the image, the path of the file might be a problem
        BufferedImage wm = null;
       try {
            wm = ImageIO.read(new File("media\\watermark\\watermark.jpg"));
        } catch (IOException e) {System.out.println(e);
        }
        // add a watermark of your choice and paste it to the image
        // e.g. text or a graphic
        //here ill try to rotate with affine transform
        AffineTransform transformer= new AffineTransform();
        BufferedImage rotated=null;
        if(img.getWidth()<img.getHeight()){
        	AffineTransform rotator= new AffineTransform();
            rotated= new BufferedImage(nwidth, nheight, BufferedImage.TYPE_INT_RGB);
        	Graphics2D r1 = rotated.createGraphics();
            rotator.translate((nwidth-owidth)/2,(nheight-oheight)/2);      	
        	rotator.rotate(Math.toRadians(90),owidth/2,oheight/2);
        	r1.setTransform(rotator);
        	r1.drawRenderedImage(img, null);
        	r1.dispose();
        }
        transformer.scale(scale, scale);
        BufferedImage affine= new BufferedImage(scalenwidth, scalenheight, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g1 = affine.createGraphics();
        g1.setTransform(transformer);
        if(img.getWidth()<img.getHeight()){g1.drawRenderedImage(rotated, null);}
        else{
        g1.drawRenderedImage(img,null);}
        g1.dispose();
        Graphics2D g2 = affine.createGraphics();
        g2.drawRenderedImage(wm, null);
        g2.dispose();
        
        
        // encode and save the image
        try{
        ImageIO.write(affine, "png", outputFile);
        } catch (IOException e) {       	
        }
        return outputFile;
        */
        
        /**
         *IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
         */
         
        
        //from here i tried using Graphics 2D to rotate the image
        /*
        Graphics2D w = img.createGraphics();
        w.drawRenderedImage(img, null);


        
        
        BufferedImage thumb= new BufferedImage(nwidth, nheight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = thumb.createGraphics();

       
        // rotate if needed
        if(img.getWidth()<img.getHeight()){
        	g.translate((nwidth-owidth)/2, (nheight-oheight)/2);
        	g.rotate(Math.toRadians(90), owidth/2, oheight/2);
        }
        g.scale(scale, scale);
        g.drawRenderedImage(img, null);
        Graphics2D t = thumb.createGraphics();
        t.drawRenderedImage(wm,null);
        t.dispose();
        g.dispose();
                // encode and save the image
        try{
        ImageIO.write(affine, "png", outputFile);
        } catch (IOException e) {       	
        }
        return outputFile;
       */

        // scale the image to a maximum of [ 200 w X 100 h ] pixels - do not distort!
        // if the image is smaller than [ 200 w X 100 h ] - print it on a [ dim X dim ] canvas!



        /**
            ./ant.sh ImageThumbnailGenerator -Dinput=media/img/ -Doutput=test/ -Drotation=90
        */
    }

    /**
        Main method. Parses the commandline parameters and prints usage information if required.
    */
    public static void main( String[] args ) throws Exception
    {
        if ( args.length < 2 ) {
            System.out.println( "usage: java itm.image.ImageThumbnailGenerator <input-image> <output-directory>" );
            System.out.println( "usage: java itm.image.ImageThumbnailGenerator <input-directory> <output-directory>" );
            System.exit( 1 );
        }
        File fi = new File( args[0] );
        File fo = new File( args[1] );

        ImageThumbnailGenerator itg = new ImageThumbnailGenerator();
        itg.batchProcessImages( fi, fo, true );
    }    
}