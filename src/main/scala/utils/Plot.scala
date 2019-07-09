package utils

import java.io.File

import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.{BoxPlot, LinePlot}
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import fitting.parameters.Sample
import scalismo.sampling.evaluators.ProductEvaluator

import scala.collection.immutable

object Plot {
  def plotParameters(samplesASM: immutable.IndexedSeq[Sample], file: File) = {
    val numCoeffs = samplesASM.head.parameters.modelCoefficients.length
    val coeffs: Seq[Seq[Double]] = for (i <- 0 until numCoeffs) yield {
      for (s <- samplesASM) yield {
        s.parameters.modelCoefficients.data(i)
      }
    }


    val plot = BoxPlot(coeffs)
      .standard(xLabels = (1 to numCoeffs).map(_.toString))
      .ybounds(-3.0, 3.0)
      .render()
      .asBufferedImage
    javax.imageio.ImageIO.write(plot, "png", file)

  }

  def plotTrace(samplesASM: immutable.IndexedSeq[Sample], asmPosteriorEvaluator: ProductEvaluator[Sample], file: java.io.File) = {

    val logValue = samplesASM.map(asmPosteriorEvaluator.logValue)

    val plot = LinePlot(logValue.zipWithIndex.map { case (v, i) => Point(i.toDouble, v) })
      .render()
      .asBufferedImage
    javax.imageio.ImageIO.write(plot, "png", file)
  }
}
