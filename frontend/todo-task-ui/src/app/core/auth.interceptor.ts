import {HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {AuthService} from './auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
	if (request.url.startsWith('assets/')) {
		return next(request);
	}
	const token = inject(AuthService).accessToken;
	return next(token ? request.clone({setHeaders: {Authorization: `Bearer ${token}`}}) : request);
};
