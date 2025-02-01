package org.j3lsmp.chessengine;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Page controller for org.j3lsmp.chessengine
 * 
 * @author Jaden
 * @since 0.0.1
 */
@Controller
class PageController {
	
	/**
	 * Home page
	 * @return "index.html"
	 */
	@GetMapping("/")
	public String index() {
		return "index.html";
	}
}