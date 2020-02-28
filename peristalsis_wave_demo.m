%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% peristalsis_wave_demo.m
% Written by Ravi Kedarasetti, 
% Calculate patricle trajectories and make a particle tracking video
% This code is for creating a 2D demo of peristalsis mechanism with a
% periodic peristaltic wave
% To be used for the output of Peristalsis_wave_2d.class
%
% Written by Ravi Kedarasetti (2020), department of Engineering Science and
% Mechanics, Pennsylvania State University 
%%
clear all
close all
clc
%% Problem parameters
Lo = 1e-3;%m
uo = Lo;%m
tau = 1; % s
g1 = 1;
g2 = 1;
g3 = 1;
tstep = tau/100; %s
tstop = 10;
reps = 2; 
disp_scale_x =  uo/Lo/g1;
disp_scale_y =  uo/Lo/g2;
%% read the data
fname= 'wave2dResults.txt';
the_data = dlmread(fname);
%% Create a grid 
nx = 201; % no of points in x
ny = 21; % no of points in y
xgrid = the_data(1:nx,1);
ygrid = the_data(1:nx:end,2); 
[Xgrid, Ygrid] = meshgrid(xgrid, ygrid);
n_frames = (size(the_data,2)-2)/4;
%% Reshape the data using the grid
umx_data = reshape(the_data(:, 3:4:end),nx,ny,n_frames);
umy_data = reshape(the_data(:, 4:4:end),nx,ny,n_frames);
xcdotx_data = reshape(the_data(:, 5:4:end),nx,ny,n_frames);
xcdoty_data = reshape(the_data(:, 6:4:end),nx,ny,n_frames);
%% Repeat the periodic data
umx_data = repmat(umx_data,1,1,reps);
umy_data = repmat(umy_data,1,1,reps);
xcdotx_data = repmat(xcdotx_data,1,1,reps);
xcdoty_data = repmat(xcdoty_data,1,1,reps);
n_frames = n_frames*reps;
%% Create a particle tracking grid
partgrid_x = 5.0:0.25:7.5;
partgrid_y = 0.2:0.2:0.8;
[partgrid_X, partgrid_Y] = meshgrid(partgrid_x, partgrid_y);
partgrid_X = partgrid_X(:);
partgrid_Y = partgrid_Y(:);
%% Calculate particle trajectories using forward euler time integration
xm = zeros(n_frames, 2, length(partgrid_X));
parfor n = 1:length(partgrid_X)
    xm(:,:,n) =forward_euler_2d([partgrid_X(n), partgrid_Y(n)], Xgrid,Ygrid, umx_data, umy_data,xcdotx_data, xcdoty_data,tstep,uo,disp_scale_x, disp_scale_y);
end
%% Make a video of particle tracking
fig = figure();
set(fig, 'Position',[1 1 1500 300])
v = VideoWriter( 'wave2d.avi', 'Uncompressed AVI');
open(v);
for n = 1:2:n_frames
    
    fill([xgrid + disp_scale_x*umx_data(:,1,n) ; flipud(xgrid + disp_scale_x*umx_data(:,end,n)) ], ...
        [(ygrid(1) + disp_scale_y*umy_data(:,1,n)) ; flipud((ygrid(end) + disp_scale_y*umy_data(:,end,n))) ], ...
        [1.0 1.0 1.0], 'LineWidth',2)
    axis([-0.5 11 -0.2 1.2])
    axis equal
    hold on
    set(gcf, 'Color','w')
    a = squeeze(xm(n,:,:));
    scatter(a(1,:), a(2,:), [], 'b', 'filled');

    colormap(winter)
    axis off
    plot([4.9 4.9], [-0.1 1.1],'k--')
    plot([7.6 7.6], [-0.1 1.1],'k--')
        
    writeVideo(v,getframe(gcf));
    
    hold off

    if n == 1 
        saveas(fig,  'Fig S1a1.png')
    end
    
    if n == 115 
        saveas(fig,  'Fig S1a2.png')
    end
    
    if n == 501
        saveas(fig,  'Fig S1a3.png')
    end
    
end
close(v);
