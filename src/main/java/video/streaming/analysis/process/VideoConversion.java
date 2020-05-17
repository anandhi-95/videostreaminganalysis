package video.streaming.analysis.process;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Scanner;
import javax.imageio.ImageIO;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.CvType;

import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class VideoConversion implements Serializable
{
    public void convert(String fname, byte[] barr) throws IOException
    {        
        try
        {
           /*
           File webm = new File("/home/anandhi/"+fname+".webm");
           OutputStream os = new FileOutputStream(webm);
           os.write(barr);
           os.close();          
           */
           InputStream inputStream = new ByteArrayInputStream(barr);
           File out = new File("/home/anandhi/"+fname+".mp4"); 
           OutputStream os = new FileOutputStream(out);
           FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputStream);
           Java2DFrameConverter converter = new Java2DFrameConverter();
           OpenCVFrameConverter.ToMat fconverter = new OpenCVFrameConverter.ToMat();

     // Instantiating the CascadeClassifier
      String xmlFile = "/home/anandhi/streamprocessing/src/main/resources/lbpcascade_frontalface_improved.xml";
      CascadeClassifier classifier = new CascadeClassifier(xmlFile);

      // Detecting the face in the snap
      MatOfRect faceDetections = new MatOfRect();


           grabber.start();
           FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(out, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
           recorder.setVideoCodecName("h264");
           recorder.setAudioCodecName("aac");
           recorder.setFormat("mp4");
           recorder.setFrameRate(25);
           recorder.setOption("r", "23");
           recorder.setSampleRate(grabber.getSampleRate());
           recorder.start();

           Frame frame;
           int i = 1;
           while ((frame = grabber.grabFrame()) != null) {
           
               BufferedImage  bi = converter.convert(frame); 
               Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC(3));
               byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
               mat.put(0, 0, data);

               classifier.detectMultiScale(mat, faceDetections);
               System.out.println(String.format("Detected %s faces", 
                          faceDetections.toArray().length));


      // Drawing boxes
      for (Rect rect : faceDetections.toArray()) {
         Imgproc.rectangle(
            mat,                                               // where to draw the box
            new Point(rect.x, rect.y),                            // bottom left
            new Point(rect.x + rect.width, rect.y + rect.height), // top right
            new Scalar(0, 0, 255),
            3                                                     // RGB colour
         );
      }

               //Imgcodecs.imwrite("/home/anandhi/"+(String.format("%03d",i))+".jpg", mat); i++;
               frame = fconverter.convert(mat);

               //ImageIO.write(bi,"jpg", new File("/home/anandhi/"+(String.format("%03d",i))+".jpg")); i++;
               recorder.record(frame);
               //System.out.println("frame num:"+(i++));
           }
           recorder.stop();
           grabber.stop();
        }catch(org.bytedeco.javacv.FrameRecorder.Exception e) { e.printStackTrace(); }
         catch(Exception e) { e.printStackTrace(); }
    }
} 

