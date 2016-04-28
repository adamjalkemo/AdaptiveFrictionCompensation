x = zeros(1,61);
y = zeros(1,61);
l = 0;
for i = 1000:1000:60*1000 
   l = l + 1;
   y(l) = sum(u(i+200:i+1000))/length(u(i+200:i+1000));
   x(l) = sum(phid(i+200:i+1000))/length(phid(i+200:i+1000));
end
figure 
%plot(x,y,'.')
plot([x(1:28) x(34:61)],[y(1:28) y(34:61)],'.')
%figure 
%plot(phid,u,'.')