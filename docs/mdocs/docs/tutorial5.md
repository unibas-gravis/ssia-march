
# Reconstruction of partial data

The goal in this tutorial is to reconstruct an incomplete femur shape using MCMC fitting and the PCA model we computed in the step before.

This task is **exactly the same** as the previous tutorial on Model Fitting, with the exception that the used model is now a PCA model (vs. a GP model previously) and the target is a partial bone.

To perform the reconstruction, please re-run the model fitting tutorial, using "datasets/partialBone.stl" as a target mesh and "datasets/my_pcamodel.h5" as a shape model.

```scala 
val targetMesh = MeshIO.readMesh(new File("datasets/partialBone.stl")).get

val shapeModel = StatisticalModelIO.readStatisticalMeshModel(
    new File("datasets/my_pcamodel.h5")
).get
```


```scala mdoc:invisible  
import java.awt.Color
import breeze.linalg.{DenseMatrix, DenseVector}
import fitting.evaluators.{ClosestPointEvaluator, PriorEvaluator}
import fitting.logger.Logger
import fitting.parameters.{Parameters, Sample}
import fitting.proposals.{RotationUpdateProposal, ShapeICPProposal, ShapeUpdateProposal, TranslationUpdateProposal}
import scalismo.io.{MeshIO, StatisticalModelIO}
import scalismo.sampling.algorithms.MetropolisHastings
import scalismo.sampling.evaluators.ProductEvaluator
import scalismo.sampling.proposals.MixtureProposal
import scalismo.statisticalmodel.MultivariateNormalDistribution
import scalismo.ui.api.ScalismoUI
import utils.CachedEvaluator
import scalismo.geometry.EuclideanVector
import utils.Utils._
import java.io.File
scalismo.initialize()
implicit val rng = scalismo.utils.Random(42)


val ui = ScalismoUI()

val targetMesh = MeshIO.readMesh(new File("datasets/partialBone.stl")).get

val shapeModel = StatisticalModelIO.readStatisticalMeshModel(
    new File("datasets/my_pcamodel.h5")
).get


val modelGroup = ui.createGroup("model")
val modelView = ui.show(modelGroup, shapeModel, "model")
modelView.meshView.opacity = 1.0
modelView.meshView.color = Color.RED

val targetGroup = ui.createGroup("target")
val targetView = ui.show(targetGroup, targetMesh, "target mesh")


val rotationUpdateProposal = RotationUpdateProposal(0.001)
val translationUpdateProposal = TranslationUpdateProposal(0.1)

val shapeUpdateProposal = ShapeUpdateProposal(shapeModel.rank, 0.01)


val pointwiseNoiseVariance = 1.0
val uncertainty = MultivariateNormalDistribution(
    DenseVector.zeros[Double](3),
    DenseMatrix.eye[Double](3) * pointwiseNoiseVariance
)
val shapeICPProposal = ShapeICPProposal(shapeModel, targetMesh, uncertainty, 1.0)

val poseAndShapeMixture = MixtureProposal.fromProposalsWithTransition(
    (0.2, rotationUpdateProposal),
    (0.2, translationUpdateProposal),
    (0.05, shapeICPProposal),
    (0.55, shapeUpdateProposal)
)

val priorEvaluator = CachedEvaluator(PriorEvaluator(shapeModel))
val closestPointEvaluator = CachedEvaluator(ClosestPointEvaluator(shapeModel, targetMesh, uncertainty))

val posteriorEvaluator = ProductEvaluator(
    priorEvaluator,
    closestPointEvaluator
)

val chain = MetropolisHastings(poseAndShapeMixture, posteriorEvaluator)

val initialParameters = Parameters(
    EuclideanVector(0, 0, 0),
    (0.0, 0.0, 0.0),
    DenseVector.zeros[Double](shapeModel.rank)
)
val initialSample = Sample("initial", initialParameters, computeCenterOfMass(shapeModel.mean))

val chainLogger = new Logger()
val chainIterator = visualizeSamples(
    chain.iterator(initialSample, chainLogger),  modelView
)

val samples = chainIterator.drop(500).take(1000).toIndexedSeq

val bestSample = samples.maxBy(s => posteriorEvaluator.logValue(s))
val bestMesh = shapeModel.instance(bestSample.parameters.modelCoefficients).transform(bestSample.poseTransformation)

val resultGroup = ui.createGroup("result")
ui.show(resultGroup, bestMesh, "best fit")

println(chainLogger.acceptanceRatios())

```
