
int		count()
{
		n=4*;
	int		i,n;

	for(i=n=0;i<10;i=i+1){
		if(points[i].x>=0&&points[i].y>=0)n=n+1;
		}
	return n;
}

void main()
{

	double	r,pi;
	pi=3.14;
	put_s("r=");
	r=get_d();
	put_s("perimetrul=");
	put_d(2e0*pi*r);
	put_s("aria=");
	put_d(pi*r*r);
}
