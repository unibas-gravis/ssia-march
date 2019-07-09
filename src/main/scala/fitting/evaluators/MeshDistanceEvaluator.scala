package fitting.evaluators

import fitting.parameters.Sample
import scalismo.geometry._3D
import scalismo.mesh.{MeshMetrics, TriangleMesh}
import scalismo.numerics.UniformMeshSampler3D
import scalismo.sampling.DistributionEvaluator
import scalismo.statisticalmodel.{MultivariateNormalDistribution, StatisticalMeshModel}
import utils.DecimateModel

case class MeshDistanceEvaluator(model: StatisticalMeshModel,
                                 target : TriangleMesh[_3D],
                                 uncertainty: breeze.stats.distributions.ContinuousDistr[Double]
                                )(implicit val rng : scalismo.utils.Random)
  extends DistributionEvaluator[Sample] {


  val modelDec = DecimateModel.decimate(model, 0.95)

  override def logValue(sample: Sample): Double = {

    val currModelInstance = modelDec.instance(sample.parameters.modelCoefficients).transform(sample.poseTransformation)
    val metricValue = MeshMetrics.avgDistance(target, currModelInstance)
    uncertainty.logPdf(metricValue)
  }


}