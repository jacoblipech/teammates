import { Component, Input, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import uaParser from 'ua-parser-js';
import { StatusMessageService } from '../services/status-message.service';
import { StatusMessage } from './status-message/status-message';

const DEFAULT_TITLE: string = 'TEAMMATES - Online Peer Feedback/Evaluation System for Student Team Projects';

/**
 * Base skeleton for all pages.
 */
@Component({
  selector: 'tm-page',
  templateUrl: './page.component.html',
  styleUrls: ['./page.component.scss'],
})
export class PageComponent implements OnInit {

  @Input() studentLoginUrl: string = '';
  @Input() instructorLoginUrl: string = '';
  @Input() user: string = '';
  @Input() isStudent: boolean = false;
  @Input() isInstructor: boolean = false;
  @Input() isAdmin: boolean = false;
  @Input() isValidUser: boolean = false;
  @Input() logoutUrl: string = '';
  @Input() pageTitle: string = '';
  @Input() hideAuthInfo: boolean = false;
  @Input() navItems: any[] = [];

  isCollapsed: boolean = true;
  isUnsupportedBrowser: boolean = false;
  isCookieDisabled: boolean = false;
  browser: string = '';
  messageList: StatusMessage[] = [];

  /**
   * Minimum versions of browsers supported.
   *
   * Angular browser support: https://angular.io/guide/browser-support
   *
   * Bootstrap 4 browser support: https://getbootstrap.com/docs/4.0/getting-started/browsers-devices/
   */
  minimumVersions: { [key: string]: number } = {
    Chrome: 45,
    IE: 10,
    Firefox: 40,
    Safari: 7,
    // Opera: ??
  };

  constructor(private router: Router, private route: ActivatedRoute, private title: Title,
      private statusMessageService: StatusMessageService) {
    this.checkBrowserVersion();
    this.router.events.subscribe((val: any) => {
      if (val instanceof NavigationEnd) {
        window.scrollTo(0, 0); // reset viewport
        this.messageList = [];
        let r: ActivatedRoute = this.route;
        while (r.firstChild) {
          r = r.firstChild;
        }
        r.data.subscribe((resp: any) => {
          this.pageTitle = resp.pageTitle;
          this.title.setTitle(resp.htmlTitle || DEFAULT_TITLE);
        });
      }
    });
  }

  private checkBrowserVersion(): void {
    const browser: any = uaParser(navigator.userAgent).browser;
    this.browser = `${browser.name} ${browser.version}`;
    this.isUnsupportedBrowser = !this.minimumVersions[browser.name]
        || this.minimumVersions[browser.name] > parseInt(browser.major, 10);
    this.isCookieDisabled = !navigator.cookieEnabled;
  }

  ngOnInit(): void {
    this.statusMessageService.getAlertEvent().subscribe((result: StatusMessage) => {
      this.messageList.push(result);
    });
  }

}
