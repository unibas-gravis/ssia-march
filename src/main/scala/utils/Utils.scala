package utils

import fitting.parameters.Sample
import scalismo.geometry.{Point, _3D}
import scalismo.mesh.TriangleMesh
import scalismo.ui.api.StatisticalMeshModelViewControls

object Utils {


  def computeCenterOfMass(mesh: TriangleMesh[_3D]): Point[_3D] = {
    val normFactor = 1.0 / mesh.pointSet.numberOfPoints
    mesh.pointSet.points.foldLeft(Point(0, 0, 0))((sum, point) =>
      sum + point.toVector * normFactor)
  }


  def visualizeSamples(iterator: Iterator[Sample], modelView: StatisticalMeshModelViewControls): Iterator[Sample] = {
    for ((sample, iteration) <- iterator.zipWithIndex) yield {

      if (iteration % 50 == 0) {
        println("iteration " + iteration)
        modelView.shapeModelTransformationView.shapeTransformationView.coefficients = sample.parameters.modelCoefficients
        modelView.shapeModelTransformationView.poseTransformationView.transformation = sample.poseTransformation
      }
      sample
    }
  }
}
