// require modules
const fs = require('fs');
const os = require('os');
const path = require('path');
const https = require('https');
const decompressZip = require('decompress-zip');

// close on exception
process.on('uncaughtException', (error) => {
   console.error(error.stack || error.message || error);
   console.error('An unexpected error has occured. Please try running the command again.');
   process.argv.length > 2 ? process.exit(1) : setInterval(() => {});
});

// close on rejection
process.on('unhandledRejection', (error) => {
   console.error(error.stack || error.message || error);
   console.error('An unexpected error has occured. Please try running the command again.');
   process.argv.length > 2 ? process.exit(1) : setInterval(() => {});
});

// just some tools
const tools = {
   // progress bar generator
   progress (total) {
      let current = 0;
      return {
         get value () {
            return current;
         },
         set value (value) {
            current = value > total ? total : value < 0 ? 0 : value;
            const progress = Math.floor(current / total * 1000);
            const dots = Math.floor(progress / 20);
            let percent = (progress / 10).toString();
            percent.includes('.') || (percent += '.0');
            process.stdout.write(`[ ${new Array(dots + 1).join('#')}${new Array(51 - dots).join('.')} ] ${percent}%\r`);
         },
         done () {
            process.stdout.write('\r\n');
         }
      };
   },
   // remote zip file -> local folder
   async transfer ({ source, destination }) {
      return await new Promise((resolve, reject) => {
         https.get(source, async (response) => {
            switch (response.statusCode) {
               case 200:
                  const length = Number(response.headers['content-length']);
                  const progress = tools.progress(length * 2);
                  response.on('data', (chunk) => {
                     progress.value += chunk.length;
                  });
                  response.on('error', (error) => {
                     progress.done();
                     reject(error);
                  });
                  response.on('end', () => {
                     const unzip = new decompressZip(`${destination}.zip`);
                     unzip.on('progress', (index, total) => {
                        progress.value = length + length / total * index;
                     });
                     unzip.on('error', (error) => {
                        progress.done();
                        reject(error);
                     });
                     unzip.on('extract', () => {
                        progress.done();
                        resolve();
                     });
                     unzip.extract({ path: destination });
                  });
                  response.pipe(fs.createWriteStream(`${destination}.zip`));
                  break;
               case 302:
                  tools.transfer({ source: response.headers.location, destination }).then(resolve).catch(reject);
                  break;
               default:
                  reject(`${response.statusCode} ${response.statusMessage}`);
            }
         });
      });
   }
};

// create .grakkit directory
const home = path.join(os.homedir(), '.grakkit');
fs.existsSync(home) || fs.mkdirSync(home);

// set graal version
const version = '20.2.0';

// set platform variables
const platform = {
   win32: {
      name: 'windows',
      script: 'init.cmd'
   },
   linux: {
      name: 'linux',
      script: 'init.sh'
   }
}[os.platform()];

if (!platform) {
   console.error('Your platform is currently unsupported by the Grakkit installer!');
   process.argv.length > 2 ? process.exit(1) : setInterval(() => {});
} else if (process.argv.includes('--nograal') && process.argv.includes('--noserver')) {
   console.error("You've specified --nograal and --noserver at the same time. What the fuck am I supposed to do now!?");
   process.argv.length > 2 ? process.exit(1) : setInterval(() => {});
} else {
   (async () => {
      if (!process.argv.includes('--nograal')) {
         console.log('Installing GraalVM...');
         await tools.transfer({
            source: `https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-${version}/graalvm-ce-java11-${platform.name}-amd64-${version}.zip`,
            destination: path.join(home, 'temp')
         });
         const graal = path.join(home, 'graal');
         fs.existsSync(graal) && (await fs.promises.rmdir(graal, { recursive: true }));
         fs.renameSync(path.join(home, 'temp', fs.readdirSync(path.join(home, 'temp'))[0]), graal);
      }
      if (!process.argv.includes('--noserver')) {
         console.log('Installing Server...');
         await tools.transfer({
            source: 'https://github.com/grakkit/installer/releases/download/server/server.zip',
            destination: path.join(home, 'server')
         });
         console.log(
            `Your server has been installed. Launch it with "${path.join(home, `server/${platform.script}`)}"`
         );
      } else {
         console.log(
            `GraalVM has been installed. Replace your server's java path with "${path.join(home, 'graal/bin/java')}"`
         );
      }
      process.argv.length > 2 ? process.exit(0) : setInterval(() => {});
   })();
}
