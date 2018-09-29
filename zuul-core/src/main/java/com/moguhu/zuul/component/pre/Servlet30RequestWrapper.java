package com.moguhu.zuul.component.pre;

import com.moguhu.zuul.http.HttpServletRequestWrapper;

import javax.servlet.http.HttpServletRequest;

/**
 * Servlet 3.0 兼容 wrapper.
 */
class Servlet30RequestWrapper extends HttpServletRequestWrapper {
	private HttpServletRequest request;

	Servlet30RequestWrapper(HttpServletRequest request) {
		super(request);
		this.request = request;
	}

	/**
	 * zuul 1.2.2 有一个BUG, HttpServletRequestWrapper.getRequest 返回一个 wrapped request 而不是原生的.
	 * @return 原生的 HttpServletRequest
	 */
	@Override
	public HttpServletRequest getRequest() {
		return this.request;
	}
}
