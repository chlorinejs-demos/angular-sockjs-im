npm run-script compile
uglifyjs public/js/app.js -c -m -o public/js/app.min.js
mv public/js/app.min.js public/js/app.js
uglifyjs app.js -c -m -o app.min.js
mv app.min.js app.js
