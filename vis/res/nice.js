class Nice {
	load(src, callback){
		var script = document.createElement('script');
		script.onload = function () {
			callback();
		};
		script.src = src;
		document.head.appendChild(script);
	}

	plot(container){
		return new Plot(container);
	}

	graph(container){
		return new Graph(container);
	}
 }

class Plot {
	constructor(container){
		this.container = document.getElementById(container);
		this.bounds = [0, 1, 0, 1]; // minX maxX minY maxY
		this.axisLabels = ['', '']; // xLabel yLabel
		this.margings = [50, 50, 50, 50]; // left bottom right top
		this.background = '#eeeeee';
		
		this.svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
		this.width = this.container.offsetWidth;
		this.height = this.container.offsetHeight;

		this.clipped_width = this.width - this.margings[0] - this.margings[2];
		this.clipped_height = this.height - this.margings[1] - this.margings[3];

		this.svg.setAttribute("width", this.width);
		this.svg.setAttribute("height", this.height);
		this.container.appendChild(this.svg);

		this.masks = [this.rect('left_mask', 0, 0, this.margings[0], this.height, '#ffffff', 1, false),
					  this.rect('right_mask', this.width - this.margings[2], 0, this.margings[2], this.height, '#ffffff', 1, false),
					  this.rect('top_mask', 0, 0, this.width, this.margings[3], '#ffffff', 1, false),
					  this.rect('bottom_mask', 0, this.height - this.margings[1], this.width, this.margings[1], '#ffffff', 1, false)];
		
		this.initAxes();
		
		
		this.groupColors = {};

		this.dots = [];
		this.rects = [];

		this.rebound();
		this.reMask();

		
	}

	format_minimal(x){
		var neg = false;
		if(x < 0){
			x = -x;
			neg = true;
		}

		if(x >= 1 && x < 10){
			x = x.toFixed(2);
		}else if(x >= 10 && x < 100){
			x = x.toFixed(1);
		}else if(x >= 100 && x < 1000){
			x = x.toFixed(0);
		}
		else if(x >= 1000 && x < 10000){
			x = (x / 1000.0).toFixed(2) + 'K';
		}else if(x >= 10000 && x < 100000){
			x = (x / 1000.0).toFixed(1) + 'K';
		}else if(x >= 100000 && x < 1000000){
			x = (x / 1000.0).toFixed(0) + 'K';
		}
		else if(x >= 1000000 && x < 10000000){
			x = (x / 1000000.0).toFixed(2) + 'M';
		}else if(x >= 10000000 && x < 100000000){
			x = (x / 1000000.0).toFixed(1) + 'M';
		}else if(x >= 100000000 && x < 1000000000){
			x = (x / 1000000.0).toFixed(0) + 'M';
		}
		else if(x >= 1000000000 && x < 10000000000){
			x = (x / 1000000000.0).toFixed(2) + 'G';
		}else if(x >= 10000000000 && x < 100000000000){
			x = (x / 1000000000.0).toFixed(1) + 'G';
		}else if(x >= 100000000000 && x < 1000000000000){
			x = (x / 1000000000.0).toFixed(0) + 'G';
		}
		else if(x >= 1000000000000 && x < 10000000000000){
			x = (x / 1000000000000.0).toFixed(2) + 'T';
		}else if(x >= 10000000000000 && x < 100000000000000){
			x = (x / 1000000000000.0).toFixed(1) + 'T';
		}else if(x >= 100000000000000){
			x = (x / 1000000000000.0).toFixed(0) + 'T';
		}


		else if(x < 1 && x >= 0.01){
			x = x.toFixed(2);
		}
		else if(x < 0.01 && x >= 0.00001){
			x = (x * 1000.0).toFixed(2) + 'm';
		}
		else if(x < 0.00001 && x >= 0.00000001){
			x = (x * 1000000.0).toFixed(2) + 'u';
		}
		else if(x < 0.00000001 && x >= 0.00000000001){
			x = (x * 1000000000.0).toFixed(2) + 'n';
		}
		else if(x < 0.00000000001 && x >= 0.00000000000001){
			x = (x * 1000000000000.0).toFixed(2) + 'p';
		}





		if(neg){
			x = '-' + x;
		}
		return x;
















	}

	rebound(){
		var plot = this;
		var tmp = [this.bounds[0], this.bounds[1], this.bounds[2], this.bounds[3]]; 
		this.bounds = [Number.MAX_VALUE, Number.MIN_VALUE, Number.MAX_VALUE, Number.MIN_VALUE];
		this.dots.forEach(dot => {
			if(dot.x < plot.bounds[0]){
				plot.bounds[0] = dot.x;
			}
			if(dot.x > plot.bounds[1]){
				plot.bounds[1] = dot.x;
			}
			if(dot.y < plot.bounds[2]){
				plot.bounds[2] = dot.y;
			}
			if(dot.y > plot.bounds[3]){
				plot.bounds[3] = dot.y;
			}
		});

		this.rects.forEach(r => {
			if(r.x < plot.bounds[0]){
				plot.bounds[0] = r.x;
			}
			if(r.x + r.w > plot.bounds[1]){
				plot.bounds[1] = r.x + r.w;
			}
			if(r.y < plot.bounds[2]){
				plot.bounds[2] = r.y;
			}
			if(r.y + r.h > plot.bounds[3]){
				plot.bounds[3] = r.y + r.h;
			}
		});


		if(plot.bounds[0] == Number.MAX_VALUE){
			plot.bounds[0] = tmp[0];
		}
		if(plot.bounds[1] == Number.MIN_VALUE){
			plot.bounds[1] = tmp[1];
		}
		if(plot.bounds[2] == Number.MAX_VALUE){
			plot.bounds[2] = tmp[2];
		}
		if(plot.bounds[3] == Number.MIN_VALUE){
			plot.bounds[3] = tmp[3];
		}







		this.dots.forEach(dot => {
			var pos = plot.drawPosition(dot.x, dot.y);
			dot.circle.setAttribute('cx', pos[0]);
			dot.circle.setAttribute('cy', pos[1]);

		});

		this.rects.forEach(r => {
			var pos = plot.drawPosition(r.x, r.y);
			var scale = plot.drawScale(r.w, r.h);
			pos[1] -= scale[1];
			r.rect.setAttribute('x', pos[0]);
			r.rect.setAttribute('y', pos[1]);
			r.rect.setAttribute('width', scale[0]);
			r.rect.setAttribute('height', scale[1]);

		});

		this.xMinLabel.innerHTML = this.format_minimal(this.bounds[0]);
		this.yMinLabel.innerHTML = this.format_minimal(this.bounds[2]);
		this.xMaxLabel.innerHTML = this.format_minimal(this.bounds[1]);
		this.yMaxLabel.innerHTML = this.format_minimal(this.bounds[3]);

		var pos = this.drawPosition(0,0);

		if((this.margings[3] + this.clipped_height) - pos[1] < 10 || pos[1] - (this.margings[3]) < 10){
			pos[1] = -10;
		}

		if((this.margings[0] + this.clipped_width) - pos[0] < 10 || pos[0] - (this.margings[0]) < 10){
			pos[0] = -10;
		}

		this.xZeroLabel.setAttribute("x", pos[0]);
		this.xZeroTick.rect.setAttribute("x", pos[0] - 0.5);
		this.yZeroLabel.setAttribute("y", pos[1]);
		this.yZeroTick.rect.setAttribute("y", pos[1]-10/3.0-0.5);

	}

	drawPosition(x, y){
		var fx = (x - this.bounds[0]) / (this.bounds[1] - this.bounds[0]);
		var dx = fx * this.clipped_width + this.margings[0];
		var fy = (y - this.bounds[2]) / (this.bounds[3] - this.bounds[2]);
		var dy = this.height - this.margings[1] - fy * this.clipped_height;
		return [dx, dy];
	}

	drawScale(w, h){
		var fw = w / (this.bounds[1] - this.bounds[0]);
		var dw = fw * this.clipped_width;
		var fh = h / (this.bounds[3] - this.bounds[2]);
		var dh = fh * this.clipped_height;
		return [dw, dh];
	}

	reMask(){
		var plot = this;
		this.masks.forEach(mask => {
			this.svg.appendChild(mask.rect);
		});
		this.svg.appendChild(this.xAxis.rect);
		this.svg.appendChild(this.yAxis.rect);
		this.svg.appendChild(this.xMinLabel);
		this.svg.appendChild(this.yMinLabel);
		this.svg.appendChild(this.xMaxLabel);
		this.svg.appendChild(this.yMaxLabel);
		this.svg.appendChild(this.xZeroLabel);
		this.svg.appendChild(this.yZeroLabel);
		this.svg.appendChild(this.xMinTick.rect);
		this.svg.appendChild(this.yMinTick.rect);
		this.svg.appendChild(this.xMaxTick.rect);
		this.svg.appendChild(this.yMaxTick.rect);
		this.svg.appendChild(this.xZeroTick.rect);
		this.svg.appendChild(this.yZeroTick.rect);


	}

	initAxes(){
		this.xAxis = this.rect('xAxis', this.margings[0], this.height-this.margings[1], this.width-this.margings[0]-this.margings[2], 1, '#aaaaaa', 1, false);
		this.yAxis = this.rect('yAxis', this.margings[0], this.margings[3], 1, this.height-this.margings[1]-this.margings[3], '#aaaaaa', 1, false);

		var pos = this.drawPosition(this.bounds[0],0);
		this.xMinLabel = this.text(this.bounds[0], pos[0], this.margings[3] + this.clipped_height + 10,'middle');
		this.xMinTick = this.rect('tick', pos[0], this.margings[3] + this.clipped_height + 1,1,3,'#aaaaaa', 1,false);

		pos = this.drawPosition(0, this.bounds[2]);
		this.yMinLabel = this.text(this.bounds[2], this.margings[0] - 5, pos[1],'end');
		this.yMinTick = this.rect('tick', this.margings[0] - 3, pos[1], 3, 1, '#aaaaaa', 1, false);

		pos = this.drawPosition(this.bounds[1],0);
		this.xMaxLabel = this.text(this.bounds[1], pos[0]-1, this.margings[3] + this.clipped_height + 10,'middle');
		this.xMaxTick = this.rect('tick', pos[0]-1, this.margings[3] + this.clipped_height + 1,1,3,'#aaaaaa', 1,false);

		pos = this.drawPosition(0, this.bounds[3]);
		this.yMaxLabel = this.text(this.bounds[3], this.margings[0] - 5, pos[1],'end');
		this.yMaxTick = this.rect('tick', this.margings[0] - 3, pos[1], 3, 1, '#aaaaaa', 1, false);


		pos = this.drawPosition(0,0);
		this.xZeroLabel = this.text(0, pos[0], this.margings[3] + this.clipped_height + 10,'middle');
		this.xZeroTick = this.rect('tick', pos[0] - 0.5, this.margings[3] + this.clipped_height + 1,1,3,'#aaaaaa', 1,false);
		this.yZeroLabel = this.text(0, this.margings[0] - 5, pos[1] + 0.5,'end');
		this.yZeroTick = this.rect('tick', this.margings[0] - 3, pos[1], 3, 1, '#aaaaaa', 1, false);


	}

	dot(id, x, y, r, c, opacity = 1, appendance = true){
		var pos = this.drawPosition(x, y);
		var circle = document.createElementNS("http://www.w3.org/2000/svg", "circle");
		circle.setAttributeNS(null, "cx", pos[0]);
		circle.setAttributeNS(null, "cy", pos[1]);
		circle.setAttributeNS(null, "r", r);
		circle.setAttributeNS(null, "fill", c);
		circle.setAttribute("opacity", opacity);
		this.svg.appendChild(circle);
		var out = {"id":id, "x":x, "y":y, "r":r, "c":c, "circle":circle};
		if(appendance){
			this.dots.push(out);
		}
		return out;
	}

	text(t, x, y, anchor = 'middle', size = 10){
		var text = document.createElementNS("http://www.w3.org/2000/svg", "text");
		text.setAttributeNS(null, "x", x);
		text.setAttributeNS(null, "y", y + size / 3.0);
		text.setAttributeNS(null, "text-anchor", anchor);
		text.setAttributeNS(null, "font-size", size);
		text.setAttributeNS(null, "font-family", "Courier New");
		text.innerHTML = t;
		this.svg.appendChild(text);
		return text;
	}

	rect(id, x, y, w, h, c, opacity, appendance = true, scaled = false){
		var scale = this.drawScale(w, h);
		var pos = this.drawPosition(x, y);
		pos[1] -= scale[1];
		if(scaled){
			//console.log(pos);
		}
		if(!scaled){
			pos = [x, y];
			scale = [w, h];
		}
		var rect = document.createElementNS("http://www.w3.org/2000/svg", "rect");
		rect.setAttributeNS(null, "x", pos[0]);
		rect.setAttributeNS(null, "y", pos[1]);
		rect.setAttributeNS(null, "width", scale[0]);
		rect.setAttributeNS(null, "height", scale[1]);
		rect.setAttributeNS(null, "fill", c);
		rect.setAttribute("opacity", opacity);

		this.svg.appendChild(rect);
		var out = {"id":id, "x":x, "y":y, "w":w, "h":h, "c":c, "rect":rect};
		if(appendance){
			this.rects.push(out);
		}
		return out;
		
	}

	shape(id, points, color = "#aaaaaa", opacity = 1, appendance = true){
		
		var plot = this;

		var polygon = document.createElementNS("http://www.w3.org/2000/svg", "polygon");
		this.svg.appendChild(polygon);

		var shape = {"id":id, "points":[], "color":color, "polygon":polygon};

		points.forEach(point => {
			shape.points.push({"x":point[0], "y":point[1]});
			var p = plot.svg.createSVGPoint();
			if(appendance){
				var drawPos = plot.drawPosition(point[0], point[1]);
				p.x = drawPos[0];
				p.y = drawPos[1];
			}else{
				p.x = point[0];
				p.y = point[1];
			}
			polygon.points.appendItem(p);
		});

		polygon.setAttributeNS(null, "fill", color);
		polygon.setAttribute("opacity", opacity);

		return shape;
		
		

		
		

	}

	scatter(x_data, y_data, radius, color = null, opacity = 1){
		var plot = this;
		for (let i = 0; i < x_data.length; i++) {
			const d = [x_data[i], y_data[i]];


			var c = '#666666';
			if(color == 'group'){
				c = d.group;
			}
			if(color.length > 7 && color.length == x_data.length){
				c = color[i];
			}
			plot.dot(d.id, d[0], d[1], radius, c, opacity);
			
		}
		this.rebound();
		this.reMask();
	}

	histogram(x_data, bins = 10, color = '#aaaaee'){
		var plot = this;
		var dim = [Number.MAX_VALUE, Number.MIN_VALUE,]
		x_data.forEach(d => {
			if(d < dim[0]){
				dim[0] = d;
			}
			if(d > dim[1]){
				dim[1] = d;
			}
			
		});
		var diff = dim[1] - dim[0];
		var frag = diff / bins;
		var bin_data = [];
		for(var i=0; i<bins; i++){
			bin_data.push({"x": dim[0] + frag * i, "y": dim[0] + frag * i + frag, "h":0});
		}
		x_data.forEach(d => {
			bin_data.forEach(b => {
				if(d >= b.x && d <= b.y){
					b.h += 1;
				}
			});
		});

		
		

		bin_data.forEach(b => {
			plot.rect("bin", b.x, 0.0, b.y-b.x, b.h, color, 1, true, true);
		});



		plot.rebound();
		plot.reMask();

	}

	histogram2D(x_data, y_data, xBins = 10, yBins = 10){
		var plot = this;
		var xDim = [Number.MAX_VALUE, Number.MIN_VALUE,]
		var yDim = [Number.MAX_VALUE, Number.MIN_VALUE,]
		x_data.forEach(d => {
			if(d < xDim[0]){
				xDim[0] = d;
			}
			if(d > xDim[1]){
				xDim[1] = d;
			}
		});

		y_data.forEach(d => {
			if(d < yDim[0]){
				yDim[0] = d;
			}
			if(d > yDim[1]){
				yDim[1] = d;
			}
		});

		var xDiff = xDim[1] - xDim[0];
		var xFrag = xDiff / xBins;

		var yDiff = yDim[1] - yDim[0];
		var yFrag = yDiff / yBins;

		var bin_data = [];

		for(var i=0; i<xBins; i++){
			for(var j=0; j<yBins; j++){
				bin_data.push({"x": xDim[0] + xFrag * i, "y": yDim[0] + yFrag * j, "h":0});
			}
			
		}
		
		for (let i = 0; i < x_data.length; i++) {
			const d = {"x":x_data[i], "y":y_data[i]};
			bin_data.forEach(b => {
				if(d.x >= b.x && d.x <= b.x + xFrag && d.y >= b.y && d.y <= b.y + yFrag){
					b.h += 1;
				}
			});	
		}


		
		var max_h = 0;
		bin_data.forEach(b => {
			if(b.h > max_h){
				max_h = b.h;
			}
		});

		var bf = 120;
		bin_data.forEach(b => {
			if(b.h > 0){
				var c;
				var f = (b.h - max_h/2) / (max_h / 2);
				if(f > 0){
					c = 'rgb(' + (bf + (255.0-bf) * f) + ', ' + (bf + (255.0-bf) * (1 - f)) + ', 0)'
				}else{
					c = 'rgb(0, ' + (bf + (255.0-bf) * (1 + f)) + ', ' + (bf + (255.0-bf) * -f) + ')'
				}
				plot.rect("bin", b.x - xFrag, b.y - yFrag, xFrag * 3, yFrag * 3, c, 0.33, true, true);
			}
		});



		plot.rebound();
		plot.reMask();

	//	this.rect('top', this.margings[0], this.margings[3], this.width-this.margings[0]-this.margings[2], 1, '#aaaaaa', 1, false);
	//	this.rect('right', this.width-this.margings[2] - 1, this.margings[3], 1, this.height-this.margings[1]-this.margings[3], '#aaaaaa', 1, false);
		
		var bins = []
		for(var i=0; i<xBins; i++){
			bins.push({"x": xDim[0] + xFrag * i, "h":0});
		}
		for (let i = 0; i < x_data.length; i++) {
			const d = {"x":x_data[i], "y":y_data[i]};
			bins.forEach(b => {
				if(d.x >= b.x && d.x <= b.x + xFrag){
					b.h += 1;
				}
			});
		}
		max_h = 0;
		bins.forEach(b => {
			if(b.h > max_h){
				max_h = b.h;
			}
		});
		var points = [];
		points.push([plot.margings[0], plot.margings[3]]);
		bins.forEach(b => {
			var draw_x = plot.drawPosition(b.x + xFrag/2.0, 0)[0];
			points.push([draw_x, plot.margings[3] - (b.h / max_h) * (plot.margings[3] - 10)]);
		});
		points.push([plot.width - plot.margings[2], plot.margings[3]]);
		this.shape("xDistr", points, "#aaaaaa", 1, false);






		bins = []
		for(var i=0; i<yBins; i++){
			bins.push({"y": yDim[0] + yFrag * i, "h":0});
		}
		for (let i = 0; i < x_data.length; i++) {
			const d = {"x":x_data[i], "y":y_data[i]};
			bins.forEach(b => {
				if(d.y >= b.y && d.y <= b.y + yFrag){
					b.h += 1;
				}
			});
		}
		max_h = 0;
		bins.forEach(b => {
			if(b.h > max_h){
				max_h = b.h;
			}
		});
		var points = [];
		points.push([plot.width - plot.margings[2], plot.margings[3]]);
		bins.forEach(b => {
			var draw_y = plot.drawPosition(0, b.y + yFrag/2.0)[1];
			points.push([plot.width - plot.margings[2] + b.h / max_h * (plot.margings[2] - 10), draw_y]);
		});
		points.push([plot.width - plot.margings[2], plot.height - plot.margings[1]]);
		this.shape("yDistr", points, "#aaaaaa", 1, false);



	}

	axisTitles(xTitle, yTitle){
		var text = document.createElementNS("http://www.w3.org/2000/svg", "text");
		text.setAttributeNS(null, "x", this.margings[0] / 2.0);
		text.setAttributeNS(null, "y", this.height / 2.0);
		text.setAttributeNS(null, "text-anchor", "middle");
		text.setAttributeNS(null, "font-size", 12);
		text.setAttributeNS(null, "font-family", "Courier New");
		text.setAttributeNS(null, "transform", "rotate(-90 "+ (this.margings[0] / 2.0) +" "+ (this.height / 2.0) +")");
		text.innerHTML = yTitle;
		this.svg.appendChild(text);

		text = document.createElementNS("http://www.w3.org/2000/svg", "text");
		text.setAttributeNS(null, "x", this.width / 2.0);
		text.setAttributeNS(null, "y", this.height - this.margings[1] / 2.0);
		text.setAttributeNS(null, "text-anchor", "middle");
		text.setAttributeNS(null, "font-size", 12);
		text.setAttributeNS(null, "font-family", "Courier New");
		text.innerHTML = xTitle;
		this.svg.appendChild(text);
	}

 }

 class Graph {
	constructor(container){
		this.container = document.getElementById(container);
		this.nodes = {};
		this.links = [];

		this.svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
		this.width = this.container.offsetWidth;
		this.height = this.container.offsetHeight;
		this.svg.setAttribute("width", this.width);
		this.svg.setAttribute("height", this.height);
		this.container.appendChild(this.svg);

		this.center = {"x":this.width/2.0, "y":this.height/2.0};
		
		this.baseCenterForce = 0.0001;


		this.conservation = 0.9;
		this.linkStrength = 0.00001;

		this.include_unconnected_nodes = true;
		this.bidirectional_links_only = false;



		this.running = true;

	}

	data(d){
		var graph = this;
		d.forEach(node => {
			if(graph.nodes[node.id] == undefined){
				graph.nodes[node.id] = {"id":node.id, links:new Set(), "quality":0};
			}
			node.links.forEach(link => {
				if(graph.nodes[link] == undefined){
					graph.nodes[link] = {"id":link, links:new Set(), "quality":0};
				}
			});
			graph.nodes[node.id].links = new Set(node.links);
		});
		this.initLinkData();		
	}

	initLinkData(){
		var graph = this;
		for (var k in graph.nodes) {
			var node = graph.nodes[k];
			var other;
			node.links.forEach(j => {
				other = graph.nodes[j];
				
				
				if(other.links.has(node.id)){
					if(node.id < other){
						graph.links.push({"source":node, "target":other, "quality":2});
						node.quality = 2;
						other.quality = 2;
					}
				}else{
					graph.links.push({"source":node, "target":other, "quality":1});
					if(node.quality != 2){
						node.quality = 1;
					}
					if(other.quality != 2){
						other.quality = 1;
					}
				}
				graph.nodes[j] = other;
			});
		}

		

	}


	init(){
		var graph = this;
		if(this.bidirectional_links_only){
			var l = [];
			graph.links.forEach(link => {
				if(link.quality == 2){
					l.push(link);
				}
			});
			graph.links = l;
		}

		if(!this.include_unconnected_nodes){
			var n = {};
			for(var k in this.nodes){
				if(this.bidirectional_links_only){
					if(this.nodes[k].quality == 2){
						n[k] = this.nodes[k];
					}
				}else{
					if(this.nodes[k].quality > 0){
						n[k] = this.nodes[k];
					}
				}
			}
			this.nodes = n;
		}

		this.drawLinks(1,'#aaaaaa', 0.5);
		this.drawNodes(3,'#666666', 1);

		this.minLinkDegrees();

		this.initLabel();

		var tmp = [];
		for(var k in graph.nodes) {

			tmp.push(graph.nodes[k]);

		}
		this.nodes = tmp;



		function loop(timestamp){
			if(graph.running){
			  graph.simulate();
			}
			window.requestAnimationFrame(loop);
		  }
		  window.requestAnimationFrame(loop);

		  //graph.simulate();
		
		
	}

	initLabel(){
		this.label = document.createElementNS("http://www.w3.org/2000/svg", "rect");
		this.label.setAttributeNS(null, "x", -100);
		this.label.setAttributeNS(null, "y", -100);
		this.label.setAttributeNS(null, "rx", 2);
		this.label.setAttributeNS(null, "ry", 2);

		this.label.setAttributeNS(null, "width", 100);
		this.label.setAttributeNS(null, "height", 10);
		this.label.setAttributeNS(null, "fill", '#111111');
		this.label.setAttribute("opacity", 0.5);
		graph.svg.appendChild(this.label);

		var text = document.createElementNS("http://www.w3.org/2000/svg", "text");
		text.setAttributeNS(null, "x", -100);
		text.setAttributeNS(null, "y", -100);
		text.setAttributeNS(null, "fill", 'white');
		text.setAttribute("font-size", 10);
		text.setAttribute("font-family", "Courier New");
		

		text.textContent = "fzF";

		this.label.text = text;

		this.label.node = null;

		graph.svg.appendChild(text);
		
	}

	

	simulate(){
		this.nodeForces();
		this.linkForces();
		this.centerForce();
		this.apply();
		this.correction(5);
		this.updateNodes();
		this.repositionLinks();

		this.updateLabel();
	}

	updateLabel(){
		if(this.label.node == null){
			this.label.setAttributeNS(null, "x", -100);
			this.label.setAttributeNS(null, "y", -100);
			this.label.text.setAttributeNS(null, "x", -100);
			this.label.text.setAttributeNS(null, "y", -100);
		}else{
			this.label.setAttributeNS(null, "x", this.label.node.x + 5);
			this.label.setAttributeNS(null, "y", this.label.node.y - 5);
			this.label.text.setAttributeNS(null, "x", this.label.node.x + 5 + 3);
			this.label.text.setAttributeNS(null, "y", this.label.node.y + 5 - 2);

			this.label.setAttributeNS(null, "width", 3 + this.label.node.id.length * 6 + 3);
			this.label.text.textContent = this.label.node.id;

		}
	}
	
	

	drawNodes(r, c, a){
		var graph = this;
		for (var k in graph.nodes) {
			var node = graph.nodes[k];
			var pos = [(Math.random()-0.5)*graph.width,(Math.random()-0.5)*graph.height];

			node.x = graph.center.x + pos[0];
			node.y = graph.center.y + pos[1];

			node.size = r ;//* Math.sqrt(node.links.size + 1);

			var circle = document.createElementNS("http://www.w3.org/2000/svg", "circle");
			circle.setAttributeNS(null, "cx", node.x);
			circle.setAttributeNS(null, "cy", node.y);
			circle.setAttributeNS(null, "r", r);
			circle.setAttributeNS(null, "fill", c);
			circle.setAttribute("opacity", a);
			circle.node = node;
			circle.addEventListener("mouseenter", function(e) {
				graph.label.node = e.currentTarget.node;
				graph.label.active = true;
			});

			circle.addEventListener("mouseout", function(e) {
				graph.label.active = false;
				setTimeout(function () {
						if(!graph.label.active){
							graph.label.node = null;
						}
						


					
				}, 2000);
				
			});

			graph.svg.appendChild(circle);

			node.circle = circle;

			

			node.force = {"x":0, "y":0};
			node.velocity = {"x":0, "y":0};
			node.acceleration = {"x":0, "y":0};

			
		}
	}

	drawLinks(w, c, a){
		var graph = this;
		this.links.forEach(link => {
			var rect = document.createElementNS("http://www.w3.org/2000/svg", "rect");
				rect.setAttributeNS(null, "height", w);
				rect.setAttributeNS(null, "fill", c);
				rect.setAttribute("opacity", a);
				graph.svg.appendChild(rect);
				link.width = w;
				link.rect = rect;
		});
	}
	
	repositionLinks(){
		this.links.forEach(link => {
			var r = link.rect;
			var src_pos = [link.source.x, link.source.y];
			var dst_pos = [link.target.x, link.target.y];

			var diff = [dst_pos[0] - src_pos[0], dst_pos[1] - src_pos[1]];
			var mag = Math.sqrt(diff[0] * diff[0] + diff[1] * diff[1]);
			var angle = (Math.atan2(diff[1], diff[0])) * 180 / Math.PI;

			r.setAttribute("width", mag);
			r.setAttribute("x", src_pos[0]);
			r.setAttribute("y", src_pos[1] - link.width/2.0);
			r.setAttribute("transform", "rotate("+ angle +", "+ src_pos[0] +", "+ src_pos[1] +")");
			
		});
	}

	minLinkDegrees(){
		this.links.forEach(link => {
			link.min_degree = Math.min(link.source.links.size, link.target.links.size);
		});
	}

	nodeForces(){
		var graph = this;
		var len = Object.keys(graph.nodes).length;
		for (let i = 0; i < len; i++) {
			graph.nodes[i].force = {"x":0, "y":0};
		}
		
		var a;
		var b;
		var diff;
		var dist_factor;
		var force;
		var size_factor;
		var graph = this;
		for (let i = 0; i < len; i++) {
		  a = this.nodes[i];
		  for (let j = i+1; j < len; j++) {
			b = this.nodes[j];
	
			diff = [a.x-b.x, a.y-b.y];
			dist_factor = diff[0] * diff[0] + diff[1] * diff[1];
			dist_factor *= dist_factor;
			size_factor = 10000 * (a.size * a.size) * (b.size * b.size) * 0.1;
	
			force = [diff[0] / dist_factor * size_factor, diff[1] / dist_factor * size_factor];
			a.force = {"x":a.force.x + force[0], "y":a.force.y + force[1]};
			b.force = {"x":b.force.x - force[0], "y":b.force.y - force[1]};
		  }  
		}
	}

	linkForces(){
		var graph = this;
		var a;
		var b;
		var diff;
		var dist_factor;
		var force;
		var strength;
		this.links.forEach(link => {
		  a = link.source;
		  b = link.target;
	
		  diff = [a.x-b.x, a.y-b.y];
		  dist_factor = diff[0] * diff[0] + diff[1] * diff[1];
		  
		  strength = 1 * graph.linkStrength * 0.001;
	
		 
		  force = [diff[0] * dist_factor * strength, diff[1] * dist_factor * strength];

		  a.force = {"x":a.force.x - force[0], "y":a.force.y - force[1]};
		  b.force = {"x":b.force.x + force[0], "y":b.force.y + force[1]}; 

		});
	}

	correction(max){
		var smax = max * max;
		var dist;
		var d;
		this.nodes.forEach(node => {
		  dist = node.velocity.x * node.velocity.x + node.velocity.y * node.velocity.y;
		  if(dist > smax){
			d = Math.sqrt(dist);
			node.velocity = {"x":node.velocity.x / d * max, "y":node.velocity.y / d * max};
			
		  }
		});
	}

	apply(){
		var graph = this;
		this.nodes.forEach(node => {
		  node.velocity.x = node.velocity.x * graph.conservation + node.force.x;
		  node.velocity.y = node.velocity.y * graph.conservation + node.force.y;
		
	
		});
	}

	centerForce(){
		var graph = this;
		this.nodes.forEach(node => {
		  var diff = [node.x - graph.center.x, node.y - graph.center.y];
		  var force = [-diff[0] * graph.baseCenterForce, -diff[1] * graph.baseCenterForce];
		  node.force.x += force[0]; 
		  node.force.y += force[1]; 
		});
	  }

	updateNodes(){
		this.nodes.forEach(node => {
			node.x += node.velocity.x;
			node.y += node.velocity.y;
			node.circle.setAttribute("cx", node.x);
			node.circle.setAttribute("cy", node.y);
		});
	}
}
		

 nice = new Nice();
