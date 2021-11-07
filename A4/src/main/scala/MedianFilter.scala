// The following script was developed by Osvaldo Aquino - 802176370
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


object Servers extends App
{

  var inputImage: BufferedImage = ImageIO.read(new File("Images/inputImage.jpg"))
  val windowWidth = 256
  val windowHeight = 256
  var sOutput = new sMedianFilter()
  var pOutput = new pMedianFilter()

  val sFuture = Future 
  {
    sOutput.SOperator(inputImage, windowWidth, windowHeight)
  }
  val pFuture = Future
  {
    pOutput.POperator(inputImage, windowWidth, windowHeight)
  }
  Await.result(sFuture, Duration.Inf)
  Await.result(pFuture, Duration.Inf)
}


class sMedianFilter
{

  def SOperator(inputImage: BufferedImage, windowWidth: Int, windowHeight: Int): Unit =
    {

    val t1 = System.nanoTime() //value to handle runtime
    val edgeX = windowWidth / 2
    val edgeY = windowHeight / 2
    val width = inputImage.getWidth()
    val height = inputImage.getHeight()
    var filteredImg: BufferedImage = inputImage // initially same as input
    var outputPixelValue = Array.ofDim[Int](width, height)
    var window =  Array.ofDim[Int](windowWidth * windowHeight)

    for(x <- edgeX to width - edgeX)
    {
      for(y <- edgeY to height - edgeY)
      {
        var i = 0
        for(fx <- 1 to windowWidth)
        {
          for(fy <- 1 to windowHeight)
          {
            try
              {
              window(i) = inputImage.getRGB(x + fx - edgeX - 1 , y + fy - edgeY - 1)
              i += 1
            }
            catch 
              {
              case e: ArrayIndexOutOfBoundsException => println("pixel ignored")
            }
          }
        }
        try
          {
          window.sortInPlace()
          outputPixelValue(x-1)(y-1) = window(windowWidth * windowHeight / 2)
          filteredImg.setRGB(x-1, y-1, window(windowWidth * windowHeight / 2))
        }
        catch
          {
          case e: ArrayIndexOutOfBoundsException => println("pixel ignored")
        }
      }
    }
    for(x <- 0 to edgeX - width)
    {
      for(y <- 0 to edgeY - height)
      {
        filteredImg.setRGB(x,y,outputPixelValue(x)(y))
      }
    }
    ImageIO.write(filteredImg, "jpg", new File("Out/serialFilteredImage.jpg"))
    val duration = (System.nanoTime() - t1) / 1e9d // Exact filtering time
    println("Serial Median Filter runtime: " + duration + " seconds")
  }
}

class pMedianFilter{

  def POperator(inputImage: BufferedImage, windowWidth: Int, windowHeight: Int): Unit ={

    val t1 = System.nanoTime() //value to handle runtime
    val edgeX = windowWidth / 2
    val edgeY = windowHeight / 2
    val width = inputImage.getWidth()
    val height = inputImage.getHeight()
    var filteredImg: BufferedImage = inputImage // initially same as input
    var window =  Array.ofDim[Int](windowWidth * windowHeight)

    for(x <- edgeX to width - edgeX)
    {
      for(y <- edgeY to height - edgeY)
      {
        var i = 0
        for(fx <- 1 to windowWidth)
        {
          for(fy <- 1 to windowHeight)
          {
            try
              {
              window(i) = inputImage.getRGB(x + fx - edgeX - 1 , y + fy - edgeY - 1)
              i += 1
            }
            catch 
              {
              case e: ArrayIndexOutOfBoundsException => println("pixel ignored")
            }
          }
        }
        try
          {
          window.sortInPlace()
          filteredImg.setRGB(x-1, y-1, window(windowWidth * windowHeight / 2))
        }
        catch
          {
          case e: ArrayIndexOutOfBoundsException => println("pixel ignored")
        }
      }
    }
    ImageIO.write(filteredImg, "jpg", new File("Out/parallelFilteredImage.jpg"))
    val duration = (System.nanoTime() - t1) / 1e9d // Exact filtering time
    println("Parallel Median Filter runtime: " + duration + " seconds")
  }
}