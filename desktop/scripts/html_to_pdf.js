/**
* Script usage syntax is: 
* node html_to_pdf.js {temp_file_path} [{--landscape}] [{--single-segment}] {segment_count}
*/


const HTML5ToPDF = require("html5-to-pdf")
const path = require("path")
 
const run = async (targetPath, number, landscape) => {
  const html5ToPDF = new HTML5ToPDF({
    inputPath: path.join(targetPath, `segment${number}.html`),
    outputPath: path.join(targetPath, `segment${number}.pdf`),
    include: [
      path.join(targetPath, "style.css"),
	  path.join(targetPath, "frontpage.png"),
    ],
	pdf: { 
	"landscape": landscape
	},
	renderDelay: 500
  })
 
  await html5ToPDF.start()
  await html5ToPDF.build()
  await html5ToPDF.close()
}
 
(async () => {
  try {
	  
	  const landscape = process.argv.includes('--landscape');
	  const singleSegment = process.argv.includes('--single-segment');
	  const targetPath = process.argv[2];
	  const segmentCount = process.argv[process.argv.length - 1];
	  if (singleSegment) {
		  await run(targetPath, segmentCount, landscape);
	  } else {
		 for (let i = 0; i < segmentCount; i++) {
		  await run(targetPath, i, landscape);
	  } 
	  }
	  

    console.log("DONE converting HTML to PDF")

  } catch (error) {
    console.error(error)
    process.exitCode = 1
  } finally {
    process.exit();
  }
})()