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

