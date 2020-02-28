clear all
close all
clc
%% Problem parameters
Lo = 1e-3;%m
c = 1e-3;%m/s
uo = Lo;%m
vo = c*uo/Lo;% m/sec
tau = 1; % s
La = 12.5e-3;%m
g1 = 1;
g2 = 1;
g3 = 1;
tstep = tau/100; %s
tstop = 10;
t_vec = 0:tstep:tstop;
%% read the data
PathName = '/gpfs/scratch/rpk5196/Comsol/Peristalsis_mechanism/';
FileName = uigetfile([PathName '*.txt'], 'select data file');
%%
fname= [PathName FileName];
the_data = dlmread(fname);
nx = 201; % no of points in x
ny = 21; % no of points in y
%%
xgrid = the_data(1:nx,1);
ygrid = the_data(1:nx:end,2); 
[Xgrid, Ygrid] = meshgrid(xgrid, ygrid);
n_frames = length(t_vec);
%%
umx_data = reshape(the_data(:, 3:4:end),nx,ny,n_frames);
umy_data = reshape(the_data(:, 4:4:end),nx,ny,n_frames);
    
    
xcdotx_data = reshape(the_data(:, 5:4:end),nx,ny,n_frames);
xcdoty_data = reshape(the_data(:, 6:4:end),nx,ny,n_frames);
    
phi = max(abs(umy_data(:)));    
%%
disp_scale_x =  uo/Lo/g1;
disp_scale_y =  uo/Lo/g2;
%% Particle tracking grid
partgrid_x = 2.5:0.25:7.5;
partgrid_y = 0.1:0.2:0.9;
[partgrid_X, partgrid_Y] = meshgrid(partgrid_x, partgrid_y);
partgrid_X = partgrid_X(:);
partgrid_Y = partgrid_Y(:);
%% Calculate particle trajectories
xm = zeros(n_frames, 2, length(partgrid_X));
for n = 1:length(partgrid_X)
    xm(:,:,n) =forward_euler_dynamic_as([partgrid_X(n), partgrid_Y(n)], Xgrid,Ygrid, umx_data, umy_data,xcdotx_data, xcdoty_data,tstep,uo,disp_scale_x, disp_scale_y);
end
%% Movie of just the walls
fig = figure();
set(fig, 'Position',[1 1 1500 300])
v = VideoWriter([PathName 'wall_movement.avi'], 'Uncompressed AVI');
open(v);
for n = 1:2:n_frames
    fill([xgrid + disp_scale_x*umx_data(:,1,n) ; flipud(xgrid + disp_scale_x*umx_data(:,end,n)) ], ...
        [(ygrid(1) + disp_scale_y*umy_data(:,1,n)) ; flipud((ygrid(end) + disp_scale_y*umy_data(:,end,n))) ] ,[0.0 0.67 1.0])
    axis([-0.5 13 -0.2 1.2])
    axis equal
    axis off
    set(gcf, 'Color','w')
%     pause(0.1)
    writeVideo(v,getframe(gcf));
    
end
close(v);
%%
fig = figure();
set(fig, 'Position',[1 1 1500 300])
v = VideoWriter([PathName FileName(1:end-4) '.avi'], 'Uncompressed AVI');
open(v);
for n = 1:2:n_frames
    fill([xgrid + disp_scale_x*umx_data(:,1,n) ; flipud(xgrid + disp_scale_x*umx_data(:,end,n)) ], ...
        [(ygrid(1) + disp_scale_y*umy_data(:,1,n)) ; flipud((ygrid(end) + disp_scale_y*umy_data(:,end,n))) ], ...
        [1.0 1.0 1.0], 'LineWidth',2)
    axis([-0.5 13 -0.2 1.2])
    axis equal
    hold on
    set(gcf, 'Color','w')
    a = squeeze(xm(n,:,:));
    scatter(a(1,:), a(2,:), [], partgrid_X*La*1e3, 'filled');

    colormap(winter)
    axis off
    plot([2 2], [-0.1 1.1],'k--')
    plot([8 8], [-0.1 1.1],'k--')
    
%     pause(0.1)
    
    writeVideo(v,getframe(gcf));
    
    hold off
end
close(v);


