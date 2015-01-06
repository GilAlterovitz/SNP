greporter.nw: index.html node_modules package.json py
	zip -r greporter.nw *
.PHONY:run,clean

run: greporter.nw
	/Applications/node-webkit.app/Contents/MacOS/node-webkit greporter.nw

clean:
	rm greporter.nw

