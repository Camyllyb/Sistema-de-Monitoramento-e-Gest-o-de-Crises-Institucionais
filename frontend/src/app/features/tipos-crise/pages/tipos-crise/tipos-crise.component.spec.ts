import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TiposCriseComponent } from './tipos-crise.component';

describe('TiposCriseComponent', () => {
  let component: TiposCriseComponent;
  let fixture: ComponentFixture<TiposCriseComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TiposCriseComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TiposCriseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
