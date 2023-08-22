const HTML5ToPDF = require("html5-to-pdf")
const path = require("path")
 
const run = async (targetPath, number) => {
  const html5ToPDF = new HTML5ToPDF({
    inputPath: path.join(targetPath, `segment${number}.html`),
    outputPath: path.join(targetPath, `segment${number}.pdf`),
    include: [
      path.join(targetPath, "style.css"),
    ],
	pdf: { "landscape": true }
  })
 
  await html5ToPDF.start()
  await html5ToPDF.build()
  await html5ToPDF.close()
}
 
(async () => {
  try {
	  const targetPath = process.argv[process.argv.length - 2];
	  const segmentCount = process.argv[process.argv.length - 1];
	  for (let i = 0; i < segmentCount; i++) {
		  await run(targetPath, i);
	  }

    console.log("DONE converting HTML to PDF")

  } catch (error) {
    console.error(error)
    process.exitCode = 1
  } finally {
    process.exit();
  }
})()