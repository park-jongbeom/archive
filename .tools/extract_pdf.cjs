// Usage: node extract_pdf.cjs <input.pdf>
// Outputs extracted text to stdout.
const { PDFParse } = require('pdf-parse');
const fs = require('fs');

(async () => {
    const inputPath = process.argv[2];
    if (!inputPath) {
        console.error('Usage: node extract_pdf.cjs <input.pdf>');
        process.exit(1);
    }
    const data = fs.readFileSync(inputPath);
    const parser = new PDFParse({ data });
    const result = await parser.getText();
    console.log('=== PAGES:', result.numpages, '===');
    console.log(result.text);
})().catch(err => {
    console.error('ERROR:', err.message);
    process.exit(1);
});
