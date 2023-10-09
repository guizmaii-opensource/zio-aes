---
id: index
title: "Getting Started with zio-AES"
sidebar_label: "Getting Started"
---

@PROJECT_BADGES@

## Introduction

Implementation of a `AES` service providing 2 functions: `::encrypt` and `::decrypt`

ZIO implementation of https://gist.github.com/guizmaii/6b5d3666081960639c3df0a24e17e2fd

## Installation

In order to use this library, we need to add the following line in our `build.sbt` file:

```scala
libraryDependencies += "com.guizmaii" %% "zio-AES" % "<version>"
```